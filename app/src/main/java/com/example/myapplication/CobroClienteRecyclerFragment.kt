package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CobroClienteRecyclerFragment : Fragment() {

    private lateinit var dbHelper: SQLite
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClienteCobroAdapter
    private lateinit var etBuscar: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_cobro_cliente_recyclerview, container, false)

        dbHelper = SQLite(requireContext())

        recyclerView = view.findViewById(R.id.rvClientes)
        etBuscar = view.findViewById(R.id.etBuscarCliente)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarClientes()

        // 🔎 BUSCADOR
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filtrar(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun cargarClientes() {
        val lista = dbHelper.obtenerClientes()
        adapter = ClienteCobroAdapter(dbHelper, lista)
        recyclerView.adapter = adapter
    }
}