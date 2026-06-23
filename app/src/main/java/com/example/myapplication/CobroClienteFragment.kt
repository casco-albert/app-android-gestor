package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CobroClienteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_cobro_cliente_recyclerview,
            container,
            false
        )

        recyclerView = view.findViewById(R.id.rvClientes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarClientes()

        return view
    }

    private fun cargarClientes() {
        RetrofitClient.api
            .getClientes()
            .enqueue(object : Callback<ApiResponse<List<Cliente>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Cliente>>>,
                    response: Response<ApiResponse<List<Cliente>>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val clientes = response.body()?.data ?: emptyList()
                        recyclerView.adapter = ClienteCobroAdapter(clientes)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al cargar clientes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Cliente>>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Sin conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}