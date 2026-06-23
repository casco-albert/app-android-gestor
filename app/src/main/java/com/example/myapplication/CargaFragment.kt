package com.example.myapplication

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.ui.RetrofitClient

class CargaFragment : Fragment() {

    private lateinit var lista: MutableList<Carga>
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_carga, container, false)

        val txtDescripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val txtFecha = view.findViewById<EditText>(R.id.editFecha)
        val txtCantidad = view.findViewById<EditText>(R.id.editCantidadTotal)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarCarga)

        listView = view.findViewById(R.id.listaCargas)

        // 🔥 FECHA ACTUAL
        val calendar = Calendar.getInstance()

        txtFecha.setText(
            "${calendar.get(Calendar.DAY_OF_MONTH)}/" +
                    "${calendar.get(Calendar.MONTH) + 1}/" +
                    "${calendar.get(Calendar.YEAR)}"
        )

        // 🔥 CALENDARIO (DATEPICKER)
        txtFecha.setOnClickListener {

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->

                    val fecha = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    txtFecha.setText(fecha)
                },
                year,
                month,
                day
            )

            datePicker.show()
        }

        // 🔥 CARGAR LISTA DESDE API
        cargarLista()

        // 🔥 GUARDAR CARGA
        btnGuardar.setOnClickListener {

            val descripcion = txtDescripcion.text.toString()
            val fecha = txtFecha.text.toString()
            val cantidad = txtCantidad.text.toString().toDoubleOrNull()

            if (descripcion.isNotEmpty() && fecha.isNotEmpty() && cantidad != null) {

                val nuevaCarga = Carga(
                    descripcion = descripcion,
                    fecha = fecha,
                    cantidadTotal = cantidad
                )

                RetrofitClient.api.insertarCarga(nuevaCarga)
                    .enqueue(object : retrofit2.Callback<ApiResponse<Carga>> {

                        override fun onResponse(
                            call: retrofit2.Call<ApiResponse<Carga>>,
                            response: retrofit2.Response<ApiResponse<Carga>>
                        ) {
                            Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show()
                            limpiar(txtDescripcion, txtCantidad)
                            cargarLista()
                        }

                        override fun onFailure(call: retrofit2.Call<ApiResponse<Carga>>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(requireContext(), "Campos inválidos", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // 🔥 LISTAR DESDE API
    private fun cargarLista() {
        RetrofitClient.api.obtenerCargas()
            .enqueue(object : retrofit2.Callback<ApiResponse<List<Carga>>> {

                override fun onResponse(
                    call: retrofit2.Call<ApiResponse<List<Carga>>>,
                    response: retrofit2.Response<ApiResponse<List<Carga>>>
                ) {
                    if (response.isSuccessful) {
                        lista = response.body()?.data?.toMutableList() ?: mutableListOf()
                        val adapter = CargaAdapter(requireContext(), lista)
                        listView.adapter = adapter
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse<List<Carga>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    // 🔥 LIMPIAR CAMPOS
    private fun limpiar(desc: EditText, cant: EditText) {
        desc.text.clear()
        cant.text.clear()
    }

}
