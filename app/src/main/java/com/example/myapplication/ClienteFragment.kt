package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClienteFragment : Fragment() {

    private lateinit var txtOrden: EditText
    private lateinit var txtNom: EditText
    private lateinit var txtDirec: EditText
    private lateinit var txtTelef: EditText
    private lateinit var txtPreciokilo: EditText
    private lateinit var btnGuardar: Button

    private val api = RetrofitClient.api

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(com.example.myapplication.R.layout.fragment_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtOrden = view.findViewById(R.id.txtRec)
        txtNom = view.findViewById(R.id.txtNom)
        txtDirec = view.findViewById(R.id.txtDirec)
        txtTelef = view.findViewById(R.id.txtTelef)
        txtPreciokilo = view.findViewById(R.id.txtPreciokilo)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            insertarCliente()
        }
    }

    private fun insertarCliente() {

        val rec = txtOrden.text.toString()
        val nom = txtNom.text.toString()
        val direc = txtDirec.text.toString()
        val telef = txtTelef.text.toString()
        val precioKilo = txtPreciokilo.text.toString()

        if (rec.isEmpty() || nom.isEmpty() || telef.isEmpty() || precioKilo.isEmpty()) {
            Toast.makeText(requireContext(), "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val cliente = Cliente(
            rec = rec.toDouble(),
            nom = nom,
            direc = direc,
            telef = telef,
            preciokilo = precioKilo.toDouble()
        )

        api.insertarCliente(cliente).enqueue(object : Callback<Cliente> {

            override fun onResponse(call: Call<Cliente>, response: Response<Cliente>) {

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Cliente guardado correctamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error en servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Cliente>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de red: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun limpiarCampos() {
        txtOrden.text.clear()
        txtNom.text.clear()
        txtDirec.text.clear()
        txtTelef.text.clear()
        txtPreciokilo.text.clear()
    }
}