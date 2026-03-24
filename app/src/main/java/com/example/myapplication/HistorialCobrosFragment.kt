package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageButton
import android.widget.TextView
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

        // 🔹 Inicializar adapter con lista vacía
        adapter = HistorialCobroAdapter(mutableListOf())
        rvHistorial.adapter = adapter

        // 🔹 Cargar datos por primera vez
        cargarDatos()

        val btnPDF = view.findViewById<ImageButton>(R.id.btnExportPDF)
        val btnExcel = view.findViewById<ImageButton>(R.id.btnExportExcel)

        btnPDF.setOnClickListener {
            val listaActual = dbHelper.obtenerHistorialCobrosTodos()
            ExportPDF.crearPDFEnDescargas(requireContext(), listaActual)
            Toast.makeText(requireContext(), "PDF generado", Toast.LENGTH_SHORT).show()
        }

        btnExcel.setOnClickListener {
            val listaActual = dbHelper.obtenerHistorialCobrosTodos()
            ExportCSV.crearCSVEnCarpeta(requireContext(), listaActual)
            Toast.makeText(requireContext(), "Excel generado", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // 🔴 FUNCIÓN DE REFRESH
    private fun cargarDatos() {
        val nuevaLista = dbHelper.obtenerHistorialCobrosTodos()
        adapter.actualizarLista(nuevaLista)
    }

    // 🔴 REFRESH AUTOMÁTICO
    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}