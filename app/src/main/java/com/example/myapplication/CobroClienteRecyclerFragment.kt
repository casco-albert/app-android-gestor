package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class CobroClienteRecyclerFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClienteCobroAdapter
    private var listaClientes = mutableListOf<Cliente>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_cobro_cliente_recyclerview, container, false)

        dbHelper = SQLite(requireContext())
        recyclerView = view.findViewById(R.id.rvClientes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarClientes()

        return view
    }

    private fun cargarClientes() {
        listaClientes = dbHelper.obtenerClientes().toMutableList()
        adapter = ClienteCobroAdapter(dbHelper, listaClientes)
        recyclerView.adapter = adapter
    }
}