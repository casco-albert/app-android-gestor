package com.example.myapplication

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentPedidosListBinding

class PedidosListFragment : Fragment() {

    private lateinit var binding: FragmentPedidosListBinding
    private lateinit var dbHelper: SQLite
    private lateinit var adapter: PedidoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPedidosListBinding.inflate(inflater, container, false)

        dbHelper = SQLite(requireContext())

        cargarPedidos()

        return binding.root
    }

    private fun cargarPedidos() {
        val lista = dbHelper.obtenerPedidos()

        adapter = PedidoAdapter(lista, dbHelper) { pedido ->
            mostrarDialogEditarCantidad(pedido)
        }

        binding.recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPedidos.adapter = adapter
    }

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

    private fun actualizarCantidad(id: Int, nuevaCantidad: Int) {

        val db = dbHelper.writableDatabase

        // obtener el cliente del pedido
        val cursor = db.rawQuery(
            "SELECT cli_id FROM pedidos WHERE id=?",
            arrayOf(id.toString())
        )

        var clienteId = 0

        if (cursor.moveToFirst()) {
            clienteId = cursor.getInt(0)
        }
        cursor.close()

        // obtener precio_kilo del cliente
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

        cargarPedidos()

        Toast.makeText(requireContext(), "Pedido actualizado correctamente!!", Toast.LENGTH_SHORT).show()
    }
}