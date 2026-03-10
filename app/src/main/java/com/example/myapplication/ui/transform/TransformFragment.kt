package com.example.myapplication.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Cliente
import com.example.myapplication.SQLite
import com.example.myapplication.databinding.FragmentTransformBinding
import android.app.AlertDialog
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.myapplication.ClienteItem
import com.example.myapplication.R
class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root = binding.root

        val recyclerView = binding.recyclerviewTransform
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Creamos adapter primero
        val adapter = ClienteAdapter(
            onEditar = { cliente ->
                mostrarDialogEditar(cliente)
                //Toast.makeText(null, "Cliente actualizado corectamente!!", Toast.LENGTH_SHORT).show()
            },
            onEliminar = { cliente ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Cliente")
                    .setMessage("¿Estás seguro que deseas eliminar a ${cliente.nombre}?")
                    .setPositiveButton("Sí") { dialog, _ ->
                        val dbHelper = SQLite(requireContext())
                        dbHelper.eliminarCliente(cliente.id)  // eliminar de SQLite
                        //cargarClientes(adapter)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        )

        recyclerView.adapter = adapter

        cargarClientes(adapter)

        return root
    }

    private fun cargarClientes(adapter: ClienteAdapter) {
        val dbHelper = SQLite(requireContext())
        val lista: List<Cliente> = dbHelper.obtenerClientes()
        adapter.submitList(lista.toList())
    }
    private fun mostrarDialogEditar(cliente: Cliente) {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_editar_cliente, null)

        val inputNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.editPrecio)

        inputNombre.setText(cliente.nombre)
        inputPrecio.setText(cliente.precioKilo.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialogInterface, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevoPrecio = inputPrecio.text.toString().toDoubleOrNull() ?: cliente.precioKilo

                val dbHelper = SQLite(requireContext())
                dbHelper.editarCliente(cliente.id, nuevoNombre, nuevoPrecio)

                // Refresca la lista en RecyclerView
                val adapter = binding.recyclerviewTransform.adapter as ClienteAdapter
                cargarClientes(adapter)

                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun cargarClientes() {
        val dbHelper = SQLite(requireContext())
        val clientes = dbHelper.obtenerClientes() // <-- crear este método en SQLite
        val listaClientes = clientes.map { ClienteItem(it.id, it.nombre, it.precioKilo) }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaClientes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerClientes = view?.findViewById<Spinner>(R.id.spinnerClientes)
        spinnerClientes?.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}