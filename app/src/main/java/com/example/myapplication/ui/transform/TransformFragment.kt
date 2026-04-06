package com.example.myapplication.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.text.TextWatcher
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.*
import com.example.myapplication.databinding.FragmentTransformBinding
import com.example.myapplication.R

class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClienteAdapter

    // 🔥 TU MODELO REAL
    private var listaOriginal: List<ClienteConPedido> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransformBinding.inflate(inflater, container, false)

        adapter = ClienteAdapter(
            onEditar = { cliente ->
                mostrarDialogEditar(cliente)
            },
            onEliminar = { cliente ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Cliente")
                    .setMessage("¿Eliminar a ${cliente.nombre}?")
                    .setPositiveButton("Sí") { dialog, _ ->
                        val dbHelper = SQLite(requireContext())
                        dbHelper.eliminarCliente(cliente.id)
                        cargarClientes()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            onClick = { item ->
                if (item.tienePedido) {
                    mostrarDialogEditarPedido(
                        item.cliente.id,
                        item.cantidadPedidos
                    )
                } else {
                    abrirFormularioPedido(item.cliente)
                }
            }
        )

        binding.recyclerviewTransform.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerviewTransform.adapter = adapter

        // 🔍 BUSCADOR
        binding.editBuscarCliente.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrar(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cargarClientes()

        return binding.root
    }

    // 🔍 FILTRO CORRECTO
    private fun filtrar(texto: String) {

        val listaFiltrada = listaOriginal.filter {
            it.cliente.nombre.contains(texto, ignoreCase = true)
        }

        adapter.submitList(listaFiltrada)
    }

    // 🚀 NAVIGATION
    private fun abrirFormularioPedido(cliente: Cliente) {
        val bundle = Bundle().apply {
            putInt("cliente_id", cliente.id)
            putString("cliente_nombre", cliente.nombre)
        }

        findNavController().navigate(
            R.id.btnNuevoPedido,
            bundle
        )
    }

    // 🔥 CARGAR CLIENTES
    private fun cargarClientes() {

        val dbHelper = SQLite(requireContext())
        val idCarga = dbHelper.obtenerUltimaCargaId()

        val lista = dbHelper.obtenerClientesConPedidos(idCarga)

        listaOriginal = lista
        adapter.submitList(lista)
    }

    // ✏️ EDITAR CLIENTE
    private fun mostrarDialogEditar(cliente: Cliente) {

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_cliente, null)

        val inputRec = dialogView.findViewById<EditText>(R.id.editRec)
        val inputNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.editPrecio)

        inputRec.setText(cliente.rec.toString())
        inputNombre.setText(cliente.nombre)
        inputPrecio.setText(cliente.precioKilo.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->

                val dbHelper = SQLite(requireContext())

                dbHelper.editarCliente(
                    cliente.id,
                    inputRec.text.toString().toDoubleOrNull() ?: cliente.rec,
                    inputNombre.text.toString(),
                    inputPrecio.text.toString().toDoubleOrNull() ?: cliente.precioKilo
                )

                cargarClientes()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔄 EDITAR PEDIDO
    private fun mostrarDialogEditarPedido(clienteId: Int, cantidadActual: Int) {

        val editText = EditText(requireContext())
        editText.setText(cantidadActual.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar cantidad pedido")
            .setView(editText)
            .setPositiveButton("Actualizar") { _, _ ->

                val nuevaCantidad = editText.text.toString().toIntOrNull() ?: return@setPositiveButton

                val db = SQLite(requireContext())
                val idCarga = db.obtenerUltimaCargaId()

                db.actualizarCantidadPorCliente(clienteId, idCarga, nuevaCantidad)

                Toast.makeText(requireContext(),
                    "Pedido actualizado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                cargarClientes()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            cargarClientes()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}