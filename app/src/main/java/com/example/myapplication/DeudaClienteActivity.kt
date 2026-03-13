package com.example.myapplication
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
class DeudaClienteActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_deuda_cliente)

        dbHelper = SQLite(this)

        val listView = findViewById<ListView>(R.id.listDeudaCliente)
        val listaDeuda = dbHelper.obtenerDeudaPorCliente()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listaDeuda
        )

        listView.adapter = adapter
    }
}