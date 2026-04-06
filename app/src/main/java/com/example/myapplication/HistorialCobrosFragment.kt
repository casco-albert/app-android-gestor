package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistorialCobrosFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialCobroAdapter
    private lateinit var spClientes: Spinner

    private var listaCobros: List<HistorialCobro> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_historial_cobros, container, false)

        dbHelper = SQLite(requireContext())

        rvHistorial = view.findViewById(R.id.rvHistorialCobros)
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())

        spClientes = view.findViewById(R.id.spClientes)

        // 🔹 Adapter
        adapter = HistorialCobroAdapter(mutableListOf())
        rvHistorial.adapter = adapter

        // 🔹 Botones
        val btnPDF = view.findViewById<ImageButton>(R.id.btnExportPDF)
        val btnExcel = view.findViewById<ImageButton>(R.id.btnExportExcel)

        btnPDF.setOnClickListener {
            val listaActual = adapter.obtenerLista()
            ExportPDF.crearPDFEnDescargas(requireContext(), listaActual)
            Toast.makeText(requireContext(), "PDF generado", Toast.LENGTH_SHORT).show()
        }

        btnExcel.setOnClickListener {
            val listaActual = adapter.obtenerLista()
            ExportCSV.crearCSVEnCarpeta(requireContext(), listaActual)
            Toast.makeText(requireContext(), "Excel generado", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Cargar datos inicial
        cargarDatos()

        return view
    }

    // 🔴 Cargar datos desde DB
    private fun cargarDatos() {
        listaCobros = dbHelper.obtenerHistorialCobrosTodos()

        adapter.actualizarLista(listaCobros)

        cargarSpinnerClientes()
    }

    // 🔽 Spinner clientes
    private fun cargarSpinnerClientes() {

        val clientes = listaCobros
            .map { Pair(it.idCliente, it.nombreCliente) }
            .distinctBy { it.first }

        val listaSpinner = mutableListOf("Todos")
        listaSpinner.addAll(clientes.map { it.second })

        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaSpinner
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spClientes.adapter = adapterSpinner

        configurarFiltroSpinner(clientes)
    }

    // 🔥 FILTRO POR CLIENTE
    private fun configurarFiltroSpinner(clientes: List<Pair<Int, String>>) {

        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val listaFiltrada = if (position == 0) {
                    listaCobros
                } else {
                    val idClienteSeleccionado = clientes[position - 1].first
                    listaCobros.filter { it.idCliente == idClienteSeleccionado }
                }

                adapter.actualizarLista(listaFiltrada)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // 🔄 REFRESH AUTOMÁTICO
    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}