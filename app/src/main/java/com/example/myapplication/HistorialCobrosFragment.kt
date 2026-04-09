package com.example.myapplication

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialCobrosFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialCobroAdapter
    private lateinit var spClientes: Spinner

    private lateinit var etFechaIni: EditText
    private lateinit var etFechaFin: EditText

    private var listaCobros: List<HistorialCobro> = listOf()
    private var clientes: List<Pair<Int, String>> = listOf()

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

        etFechaIni = view.findViewById(R.id.etFechaIni)
        etFechaFin = view.findViewById(R.id.etFechaFin)

        // 🔹 Adapter
        adapter = HistorialCobroAdapter(mutableListOf())
        rvHistorial.adapter = adapter

        // 🔹 DatePickers
        etFechaIni.setOnClickListener {
            mostrarDatePicker(etFechaIni)
        }

        etFechaFin.setOnClickListener {
            mostrarDatePicker(etFechaFin)
        }

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

        clientes = listaCobros
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

        configurarFiltros()
    }

    // 🔥 FILTROS (cliente + fechas)
    private fun configurarFiltros() {

        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltros(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etFechaIni.setOnFocusChangeListener { _, _ -> aplicarFiltros(spClientes.selectedItemPosition) }
        etFechaFin.setOnFocusChangeListener { _, _ -> aplicarFiltros(spClientes.selectedItemPosition) }
    }

    private fun aplicarFiltros(position: Int) {

        var listaFiltrada = listaCobros

        // 🔹 Filtro por cliente
        if (position != 0) {
            val idClienteSeleccionado = clientes[position - 1].first
            listaFiltrada = listaFiltrada.filter { it.idCliente == idClienteSeleccionado }
        }

        // 🔹 Filtro por fechas
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val fechaIni = etFechaIni.text.toString()
        val fechaFin = etFechaFin.text.toString()

        if (fechaIni.isNotEmpty()) {
            val fIni = formato.parse(fechaIni)
            listaFiltrada = listaFiltrada.filter {
                val f = formato.parse(it.fecha)
                f != null && fIni != null && !f.before(fIni)
            }
        }

        if (fechaFin.isNotEmpty()) {
            val fFin = formato.parse(fechaFin)
            listaFiltrada = listaFiltrada.filter {
                val f = formato.parse(it.fecha)
                f != null && fFin != null && !f.after(fFin)
            }
        }

        adapter.actualizarLista(listaFiltrada)
    }

    // 📅 DatePicker
    private fun mostrarDatePicker(editText: EditText) {
        val calendario = Calendar.getInstance()

        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->

            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaSeleccionadaStr = String.format("%02d/%02d/%04d", d, m + 1, y)
            val fechaSeleccionada = formato.parse(fechaSeleccionadaStr)

            val fechaIniStr = etFechaIni.text.toString()
            val fechaFinStr = etFechaFin.text.toString()

            // 🔴 VALIDACIÓN
            if (editText.id == R.id.etFechaFin && fechaIniStr.isNotEmpty()) {
                val fechaIni = formato.parse(fechaIniStr)

                if (fechaSeleccionada != null && fechaIni != null && fechaSeleccionada.before(fechaIni)) {
                    Toast.makeText(requireContext(), "La fecha 'Hasta' no puede ser menor que 'Desde'", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
            }

            if (editText.id == R.id.etFechaIni && fechaFinStr.isNotEmpty()) {
                val fechaFin = formato.parse(fechaFinStr)

                if (fechaSeleccionada != null && fechaFin != null && fechaSeleccionada.after(fechaFin)) {
                    Toast.makeText(requireContext(), "La fecha 'Desde' no puede ser mayor que 'Hasta'", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
            }

            // ✅ Si pasa validación
            editText.setText(fechaSeleccionadaStr)

            // 🔥 Aplicar filtros automáticamente
            aplicarFiltros(spClientes.selectedItemPosition)

        }, year, month, day)

        datePicker.show()
    }
    // 🔄 REFRESH AUTOMÁTICO
    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}