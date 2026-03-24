package com.example.myapplication

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentPedidosBinding

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)

        cargarClientes()
        generarNumeroPedido()
        // Listener al campo cantidad
        binding.editCantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                actualizarKilosYPrecio()
            }
        })

        // Listener al cambio de cliente
        binding.spinnerClientes.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                actualizarKilosYPrecio()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        binding.btnGuardarPedido.setOnClickListener {
            insertarPedido()
        }

        return binding.root
    }

    private fun insertarPedido() {

        val dbHelper = SQLite(requireContext())
        val baseDatos = dbHelper.writableDatabase

        val nroPedido = binding.editNroPedido.text.toString()
        val cantidadTexto = binding.editCantidad.text.toString()
        val clienteSeleccionado = binding.spinnerClientes.selectedItem as? ClienteItem

        if (clienteSeleccionado == null || nroPedido.isEmpty() || cantidadTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidad = cantidadTexto.toInt()
        val kilos = cantidad * 40
        val precio = kilos * clienteSeleccionado.preciokilo

        val idCarga = dbHelper.obtenerUltimaCargaId()

        // 🔴 VALIDAR SI YA EXISTE PEDIDO EN ESA CARGA
        if (dbHelper.existePedidoEnCarga(clienteSeleccionado.id, idCarga)) {

            Toast.makeText(
                requireContext(),
                "Cliente ya tiene pedido en esta Carga",
                Toast.LENGTH_LONG
            ).show()

            baseDatos.close()
            return
        }

        val registro = ContentValues().apply {
            put("nro_pedido", nroPedido)
            put("cli_id", clienteSeleccionado.id)
            put("id_carga", idCarga)
            put("cantidad", cantidad)
            put("kilos", kilos.toDouble())
            put("precio", precio)
        }

        val resultado = baseDatos.insert("pedidos", null, registro)
        baseDatos.close()

        if (resultado != -1L) {
            Toast.makeText(requireContext(), "Pedido guardado correctamente", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            generarNumeroPedido()
        } else {
            Toast.makeText(requireContext(), "Error al guardar pedido", Toast.LENGTH_SHORT).show()
        }
    }


    private fun limpiarCampos() {
        binding.editNroPedido.text.clear()
        binding.editCantidad.text.clear()
        binding.editKilos.text.clear()
        binding.editPrecio.text.clear()
    }

    private fun cargarClientes() {
        val dbHelper = SQLite(requireContext())
        val clientes = dbHelper.obtenerClientes() // Devuelve List<Cliente>

        if (clientes.isEmpty()) {
            Toast.makeText(requireContext(), "No hay clientes disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val listaClientes = mutableListOf<ClienteItem>()
        listaClientes.add(ClienteItem(0, "Seleccionar cliente", 0.0))

        clientes.forEach { cliente ->
            listaClientes.add(ClienteItem(cliente.id, cliente.nombre, cliente.precioKilo))
        }

        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            listaClientes
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerClientes.adapter = adapter
    }

    private fun actualizarKilosYPrecio() {
        val cantidadTexto = binding.editCantidad.text.toString()
        if (cantidadTexto.isEmpty()) {
            binding.editKilos.text.clear()
            binding.editPrecio.text.clear()
            return
        }

        val cantidad = cantidadTexto.toIntOrNull() ?: return
        val clienteSeleccionado = binding.spinnerClientes.selectedItem as? ClienteItem ?: return

        val kilos = cantidad * 40
        val precio = kilos * clienteSeleccionado.preciokilo

        binding.editKilos.setText(kilos.toString())
        binding.editPrecio.setText(precio.toString())
    }
    private fun generarNumeroPedido() {
        val dbHelper = SQLite(requireContext())
        val idCarga = dbHelper.obtenerUltimaCargaId()

        val ultimoNro = dbHelper.obtenerUltimoNroPedidoPorCarga(idCarga)

        val nuevoNro = ultimoNro + 1

        binding.editNroPedido.setText(nuevoNro.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}