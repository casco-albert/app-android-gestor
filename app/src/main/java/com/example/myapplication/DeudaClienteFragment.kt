package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.myapplication.SQLite

class DeudaClienteFragment : Fragment() {

    private lateinit var dbHelper: SQLite

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_deuda_cliente, container, false)

        dbHelper = SQLite(requireContext())

        val listView = view.findViewById<ListView>(R.id.listDeudaCliente)

        val listaDeuda = dbHelper.obtenerDeudaPorCliente()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listaDeuda
        )

        listView.adapter = adapter

        return view
    }
}