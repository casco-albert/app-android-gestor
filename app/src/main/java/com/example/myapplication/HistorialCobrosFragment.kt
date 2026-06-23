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
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialCobrosFragment : Fragment() {

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

        rvHistorial = view.findViewById(R.id.rvHistorialCobros)
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())

        spClientes  = view.findViewById(R.id.spClientes)
        etFechaIni  = view.findViewById(R.id.etFechaIni)
        etFechaFin  = view.findViewById(R.id.etFechaFin)

        adapter = HistorialCobroAdapter(mutableListOf())
        rvHistorial.adapter = adapter

        etFechaIni.setOnClickListener { mostrarDatePicker(etFechaIni) }
        etFechaFin.setOnClickListener { mostrarDatePicker(etFechaFin) }

        view.findViewById<ImageButton>(R.id.btnExportPDF).setOnClickListener {
            ExportPDF.crearPDFEnDescargas(requireContext(), adapter.obtenerLista())
            Toast.makeText(requireContext(), "PDF generado", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.btnExportExcel).setOnClickListener {
            ExportCSV.crearCSVEnCarpeta(requireContext(), adapter.obtenerLista())
            Toast.makeText(requireContext(), "Excel generado", Toast.LENGTH_SHORT).show()
        }

        cargarDatos()

        return view
    }

    private fun cargarDatos() {
        RetrofitClient.api
            .getCobros()
            .enqueue(object : Callback<ApiResponse<List<HistorialCobroDTO>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<HistorialCobroDTO>>>,
                    response: Response<ApiResponse<List<HistorialCobroDTO>>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        listaCobros = response.body()?.data
                            ?.map { HistorialCobroMapper.fromDTO(it) }
                            ?: emptyList()

                        adapter.actualizarLista(listaCobros)
                        cargarSpinnerClientes()

                    } else {
                        Toast.makeText(requireContext(), "Error al cargar cobros", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<HistorialCobroDTO>>>,
                    t: Throwable
                ) {
                    Toast.makeText(requireContext(), "Sin conexión: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

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

        if (position != 0) {
            val idClienteSeleccionado = clientes[position - 1].first
            listaFiltrada = listaFiltrada.filter { it.idCliente == idClienteSeleccionado }
        }

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaIni = etFechaIni.text.toString()
        val fechaFin = etFechaFin.text.toString()

        if (fechaIni.isNotEmpty()) {
            val fIni = formato.parse(fechaIni)
            listaFiltrada = listaFiltrada.filter {
                val f = formato.parse(formatearFecha(it.fecha))
                f != null && fIni != null && !f.before(fIni)
            }
        }

        if (fechaFin.isNotEmpty()) {
            val fFin = formato.parse(fechaFin)
            listaFiltrada = listaFiltrada.filter {
                val f = formato.parse(formatearFecha(it.fecha))
                f != null && fFin != null && !f.after(fFin)
            }
        }

        adapter.actualizarLista(listaFiltrada)
    }

    // Convierte timestamp segundos → dd/MM/yyyy para comparar fechas
    private fun formatearFecha(fecha: String): String {
        return try {
            if (fecha.all { it.isDigit() }) {
                val ts = fecha.toLong() * 1000
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date(ts))
            } else {
                val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val salida  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                salida.format(entrada.parse(fecha)!!)
            }
        } catch (e: Exception) { fecha }
    }

    private fun mostrarDatePicker(editText: EditText) {
        val calendario = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaSeleccionadaStr = String.format("%02d/%02d/%04d", d, m + 1, y)
            val fechaSeleccionada = formato.parse(fechaSeleccionadaStr)

            val fechaIniStr = etFechaIni.text.toString()
            val fechaFinStr = etFechaFin.text.toString()

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

            editText.setText(fechaSeleccionadaStr)
            aplicarFiltros(spClientes.selectedItemPosition)

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
            .show()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}