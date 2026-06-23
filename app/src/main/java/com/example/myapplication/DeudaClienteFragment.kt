package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeudaClienteFragment : Fragment() {

    private lateinit var listaDeuda: List<DeudaCliente>
    private lateinit var listView: ListView
    private lateinit var btnCSV: ImageButton
    private lateinit var btnPDF: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_deuda_cliente, container, false)

        listView = view.findViewById(R.id.listDeudaCliente)
        btnCSV   = view.findViewById(R.id.btnCSVDeuda)
        btnPDF   = view.findViewById(R.id.btnPDFDeuda)

        cargarDeudas()

        return view
    }

    private fun cargarDeudas() {
        RetrofitClient.api  // ← .api directo, no .instance.create()
            .getDeudas()
            .enqueue(object : Callback<ApiResponse<List<DeudaClienteDTO>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<DeudaClienteDTO>>>,
                    response: Response<ApiResponse<List<DeudaClienteDTO>>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        listaDeuda = response.body()?.data
                            ?.map { DeudaMapper.fromDTO(it) }
                            ?: emptyList()

                        listView.adapter = DeudaClienteAdapter(requireContext(), listaDeuda)

                        btnCSV.setOnClickListener {
                            ExportUtils.exportarCSVDeuda(requireContext(), listaDeuda)
                        }
                        btnPDF.setOnClickListener {
                            ExportUtils.exportarPDFDeuda(requireContext(), listaDeuda)
                        }

                    } else {
                        mostrarError("Error al cargar deudas")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<DeudaClienteDTO>>>,
                    t: Throwable
                ) {
                    mostrarError("Sin conexión: ${t.message}")
                }
            })
    }

    private fun mostrarError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}