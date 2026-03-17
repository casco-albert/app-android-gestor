package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CobroClienteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: SQLite

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_cobro_cliente_recyclerview,
            container,
            false
        )

        recyclerView = view.findViewById(R.id.rvClientes)

        dbHelper = SQLite(requireContext())

        val clientes = dbHelper.obtenerClientesConDeuda()

        val adapter = ClienteCobroAdapter(dbHelper, clientes)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }
}