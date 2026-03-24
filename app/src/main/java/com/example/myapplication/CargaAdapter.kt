package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ArrayAdapter

class CargaAdapter(
    context: Context,
    private val lista: List<Carga>
) : ArrayAdapter<Carga>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_carga, parent, false)

        val item = lista[position]

        val txtId = view.findViewById<TextView>(R.id.cargaId)
        val txtDescripcion = view.findViewById<TextView>(R.id.cargaDescripcion)
        val txtFecha = view.findViewById<TextView>(R.id.cargaFecha)
        val txtCantidad = view.findViewById<TextView>(R.id.cargaCantidad)

        txtId.text = item.id.toString()
        txtDescripcion.text = item.descripcion
        txtFecha.text = item.fecha
        txtCantidad.text = "${item.cantidadTotal}"

        return view
    }
}