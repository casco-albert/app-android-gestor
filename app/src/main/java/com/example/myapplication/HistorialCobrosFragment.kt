package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistorialCobrosFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialCobroAdapter
    private var clienteId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_historial_cobros, container, false)

        clienteId = arguments?.getInt("clienteId") ?: 0
        dbHelper = SQLite(requireContext())

        rvHistorial = view.findViewById(R.id.rvHistorialCobros)
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())

        val listaCobros = dbHelper.obtenerHistorialCobrosTodos()

        adapter = HistorialCobroAdapter(listaCobros)
        rvHistorial.adapter = adapter

        return view
    }
}