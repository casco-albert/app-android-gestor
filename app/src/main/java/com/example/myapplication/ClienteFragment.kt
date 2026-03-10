package com.example.myapplication

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class ClienteFragment : Fragment() {

    private lateinit var txtDoc: EditText
    private lateinit var txtNom: EditText
    private lateinit var txtDirec: EditText
    private lateinit var txtTelef: EditText
    private lateinit var txtPreciokilo: EditText
    private lateinit var btnGuardar: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtDoc = view.findViewById(R.id.txtDoc)
        txtNom = view.findViewById(R.id.txtNom)
        txtDirec = view.findViewById(R.id.txtDirec)
        txtTelef = view.findViewById(R.id.txtTelef)
        txtPreciokilo = view.findViewById(R.id.txtPreciokilo)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            insertar()
        }
    }

    private fun insertar() {

        val con = SQLite(requireContext())
        val baseadatos = con.writableDatabase

        val doc = txtDoc.text.toString()
        val nom = txtNom.text.toString()
        val direc = txtDirec.text.toString()
        val telef = txtTelef.text.toString()
        val precioKilo = txtPreciokilo.text.toString()

        if (doc.isNotEmpty() && nom.isNotEmpty()
            && telef.isNotEmpty() && precioKilo.isNotEmpty()) {

            val registro = ContentValues()
            registro.put("doc", doc)
            registro.put("nom", nom)
            registro.put("direc", direc)
            registro.put("telef", telef)
            registro.put("preciokilo", precioKilo.toDouble())

            val resultado = baseadatos.insert("clientes", null, registro)
            baseadatos.close()

            if (resultado != -1L) {
                Toast.makeText(requireContext(), "Cliente guardado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            } else {
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        txtDoc.text.clear()
        txtNom.text.clear()
        txtDirec.text.clear()
        txtTelef.text.clear()
        txtPreciokilo.text.clear()
    }

}