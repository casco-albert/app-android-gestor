package com.example.myapplication

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentPedidosListBinding
import java.io.File
import java.io.FileWriter

class PedidosListFragment : Fragment() {

    private lateinit var binding: FragmentPedidosListBinding
    private lateinit var dbHelper: SQLite
    private lateinit var adapter: PedidoAdapter
    private var listaCargas = listOf<Carga>()

    lateinit var txtTotalCantidad: TextView
    lateinit var txtTotalKilos: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPedidosListBinding.inflate(inflater, container, false)
        dbHelper = SQLite(requireContext())

        // Spinner
        cargarSpinnerCargas()

        // 🔹 BOTÓN PDF
        binding.btnPDF.setOnClickListener {
            crearPDFPedidos()
        }

        // 🔹 BOTÓN CSV
        binding.btnCSV.setOnClickListener {
            exportarCSVPedidos()
        }
        txtTotalCantidad = binding.txtTotalCantidad
        txtTotalKilos = binding.txtTotalKilos

        return binding.root
    }

    // 🔹 SPINNER DE CARGAS
    private fun cargarSpinnerCargas() {
        listaCargas = dbHelper.obtenerCargas()

        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaCargas.map { it.descripcion }
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCargas.adapter = adapterSpinner

        binding.spinnerCargas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val cargaSeleccionada = listaCargas[position]
                cargarPedidos(cargaSeleccionada.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // 🔹 CARGAR PEDIDOS
    private fun cargarPedidos(idCarga: Int) {
        val lista = dbHelper.obtenerPedidosPorCarga(idCarga)

        adapter = PedidoAdapter(lista, dbHelper) { pedido ->
            mostrarDialogEditarCantidad(pedido)
        }

        binding.recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPedidos.adapter = adapter
        adapter.ordenarPorNumero()

        actualizarTotales(
            lista)

    }

    // 🔹 EDITAR CANTIDAD
    private fun mostrarDialogEditarCantidad(pedido: Pedido) {
        val editText = EditText(requireContext())
        editText.setText(pedido.cantidad.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cantidad")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaCantidad = editText.text.toString().toIntOrNull()
                if (nuevaCantidad != null) {
                    actualizarCantidad(pedido.id, nuevaCantidad)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔹 ACTUALIZAR CANTIDAD
    private fun actualizarCantidad(id: Int, nuevaCantidad: Int) {

        val db = dbHelper.writableDatabase

        // 🔴 SI LA CANTIDAD ES 0 → ELIMINAR
        if (nuevaCantidad == 0) {
            db.delete("pedidos", "id=?", arrayOf(id.toString()))
            db.close()

            val cargaActual = listaCargas.getOrNull(binding.spinnerCargas.selectedItemPosition)?.id
            cargaActual?.let { cargarPedidos(it) }

            Toast.makeText(requireContext(), "Pedido eliminado", Toast.LENGTH_SHORT).show()
            return
        }

        val cursor = db.rawQuery(
            "SELECT cli_id FROM pedidos WHERE id=?",
            arrayOf(id.toString())
        )

        var clienteId = 0
        if (cursor.moveToFirst()) {
            clienteId = cursor.getInt(0)
        }
        cursor.close()

        val cursorCliente = db.rawQuery(
            "SELECT preciokilo FROM clientes WHERE id=?",
            arrayOf(clienteId.toString())
        )

        var precioKilo = 0.0
        if (cursorCliente.moveToFirst()) {
            precioKilo = cursorCliente.getDouble(0)
        }
        cursorCliente.close()

        val kilos = nuevaCantidad * 40
        val precio = kilos * precioKilo

        val values = ContentValues()
        values.put("cantidad", nuevaCantidad)
        values.put("kilos", kilos.toDouble())
        values.put("precio", precio)

        db.update("pedidos", values, "id=?", arrayOf(id.toString()))
        db.close()

        val cargaActual = listaCargas.getOrNull(binding.spinnerCargas.selectedItemPosition)?.id
        cargaActual?.let { cargarPedidos(it) }

        Toast.makeText(requireContext(), "Pedido actualizado correctamente", Toast.LENGTH_SHORT).show()
    }

    // 🔹 EXPORTAR PDF
    private fun crearPDFPedidos() {

        val posicion = binding.spinnerCargas.selectedItemPosition
        if (posicion == AdapterView.INVALID_POSITION) {
            Toast.makeText(requireContext(), "Seleccione una carga", Toast.LENGTH_SHORT).show()
            return
        }

        val idCarga = listaCargas[posicion].id
        val lista = dbHelper.obtenerPedidosPorCarga(idCarga)

        ExportPDFPedidos.crearPDFEnDescargas(requireContext(), lista)
    }
    // 🔹 EXPORTAR CSV
    private fun exportarCSVPedidos() {

        val posicion = binding.spinnerCargas.selectedItemPosition
        if (posicion == AdapterView.INVALID_POSITION) {
            Toast.makeText(requireContext(), "Seleccione una carga", Toast.LENGTH_SHORT).show()
            return
        }

        val idCarga = listaCargas[posicion].id
        val lista = dbHelper.obtenerPedidosPorCarga(idCarga)


        ExportCSVPedidos.crearCSVEnDescargas(requireContext(), lista)
    }
    private fun actualizarTotales(lista: List<Pedido>) {

        val totalCantidad = lista.sumOf { it.cantidad }
        val totalKilos = lista.sumOf { it.kilos }

        txtTotalCantidad.text = "Cant: $totalCantidad"
        txtTotalKilos.text = "Kg: %.1f".format(totalKilos)
    }
}