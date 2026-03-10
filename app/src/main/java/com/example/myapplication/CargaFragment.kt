package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.Carga
import com.example.myapplication.R
import com.example.myapplication.SQLite

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

        cargarLista()

        btnGuardar.setOnClickListener {

            val descripcion = txtDescripcion.text.toString()
            val fecha = txtFecha.text.toString()
            val cantidad = txtCantidad.text.toString()

            if (descripcion.isEmpty() || fecha.isEmpty() || cantidad.isEmpty()) {

                Toast.makeText(requireContext(), "Complete los campos", Toast.LENGTH_SHORT).show()

            } else {

                db.insertarCarga(
                    descripcion,
                    fecha,
                    cantidad.toDouble()
                )

                txtDescripcion.text.clear()
                txtFecha.text.clear()
                txtCantidad.text.clear()

                cargarLista()

                Toast.makeText(requireContext(), "Carga guardada", Toast.LENGTH_SHORT).show()
            }

        }

        return view
    }

    private fun cargarLista() {

        lista = db.obtenerCargas()

        val datos = lista.map {
            "ID: ${it.id}  |  ${it.descripcion}  |  ${it.fecha}  |  ${it.cantidadTotal} kg"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            datos
        )

        listView.adapter = adapter
    }
}