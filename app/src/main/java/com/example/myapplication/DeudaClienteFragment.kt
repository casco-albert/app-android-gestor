package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import androidx.fragment.app.Fragment

class DeudaClienteFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var listaDeuda: List<DeudaCliente>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_deuda_cliente, container, false)

        dbHelper = SQLite(requireContext())

        val listView = view.findViewById<ListView>(R.id.listDeudaCliente)
        val btnPDF = view.findViewById<ImageButton>(R.id.btnPDFDeuda)
        val btnCSV = view.findViewById<ImageButton>(R.id.btnCSVDeuda)

        // ✅ Traer datos DIRECTO como DeudaCliente
        listaDeuda = dbHelper.obtenerDeudaPorCliente()

        // ⚠️ Usá un adapter que trabaje con DeudaCliente
        val adapter = DeudaClienteAdapter(requireContext(), listaDeuda)
        listView.adapter = adapter

        // ✅ EXPORTAR
        btnCSV.setOnClickListener {
            ExportUtils.exportarCSVDeuda(requireContext(), listaDeuda)
        }

        btnPDF.setOnClickListener {
            ExportUtils.exportarPDFDeuda(requireContext(), listaDeuda)
        }

        return view
    }
}