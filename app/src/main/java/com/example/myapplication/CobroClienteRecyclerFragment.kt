package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CobroClienteRecyclerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClienteCobroAdapter
    private lateinit var etBuscar: EditText

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
        etBuscar = view.findViewById(R.id.etBuscarCliente)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarClientes()

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (::adapter.isInitialized) {
                    adapter.filtrar(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun cargarClientes() {
        RetrofitClient.api
            .getClientesConDeuda()
            .enqueue(object : Callback<ApiResponse<List<Cliente>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Cliente>>>,
                    response: Response<ApiResponse<List<Cliente>>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val clientes = response.body()?.data ?: emptyList()
                        adapter = ClienteCobroAdapter(clientes)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar clientes", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Cliente>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Sin conexión: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}