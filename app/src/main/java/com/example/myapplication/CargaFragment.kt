package com.example.myapplication

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CargaFragment : Fragment() {

    private lateinit var db: SQLite
    private lateinit var lista: MutableList<Carga>
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_carga, container, false)

        db = SQLite(requireContext())

        val txtDescripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val txtFecha = view.findViewById<EditText>(R.id.editFecha)
        val txtCantidad = view.findViewById<EditText>(R.id.editCantidadTotal)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarCarga)

        listView = view.findViewById(R.id.listaCargas)

        // 🔥 FECHA ACTUAL
        val calendar = Calendar.getInstance()

        fun setFechaActual() {
            val d = calendar.get(Calendar.DAY_OF_MONTH)
            val m = calendar.get(Calendar.MONTH)
            val y = calendar.get(Calendar.YEAR)
            txtFecha.setText("$d/${m + 1}/$y")
        }

        setFechaActual()

        // 🔥 ABRIR CALENDARIO
        txtFecha.setOnClickListener {

            val partes = txtFecha.text.toString().split("/")

            val d = partes.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
            val m = partes.getOrNull(1)?.toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
            val y = partes.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)

            val datePicker = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->

                    val fechaSeleccionada =
                        "$selectedDay/${selectedMonth + 1}/$selectedYear"

                    txtFecha.setText(fechaSeleccionada)

                }, y, m, d)

            datePicker.show()
        }

        // 🔥 CARGAR LISTA
        cargarLista()

        // 🔥 GUARDAR
        btnGuardar.setOnClickListener {

            val descripcion = txtDescripcion.text.toString().trim()
            val fecha = txtFecha.text.toString().trim()
            val cantidadStr = txtCantidad.text.toString().trim()

            val cantidad = cantidadStr.toDoubleOrNull()

            if (descripcion.isEmpty() || fecha.isEmpty() || cantidad == null) {

                Toast.makeText(
                    requireContext(),
                    "Complete los campos correctamente",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                db.insertarCarga(descripcion, fecha, cantidad)

                txtDescripcion.text.clear()
                txtCantidad.text.clear()

                // 🔥 NO BORRA FECHA → vuelve a actual
                setFechaActual()

                cargarLista()

                Toast.makeText(
                    requireContext(),
                    "Carga guardada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    private fun cargarLista() {

        lista = db.obtenerCargas()

        val adapter = CargaAdapter(requireContext(), lista)

        listView.adapter = adapter
    }
}