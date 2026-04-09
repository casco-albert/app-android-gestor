package com.example.myapplication

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.util.Locale

class ClienteCobroAdapter(
    private val dbHelper: SQLite,
    clientes: List<Cliente>
) : RecyclerView.Adapter<ClienteCobroAdapter.ViewHolder>() {

    // 🔹 Lista original (NO se toca)
    private val listaOriginal = clientes.toMutableList()

    // 🔹 Lista visible (filtrada)
    private val clientesConDeuda = clientes
        .filter { dbHelper.obtenerSaldoCliente(it.id) > 0 }
        .toMutableList()

    private val formato = DecimalFormat.getNumberInstance(Locale("es", "PY"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCliente)
        val tvDeuda: TextView = view.findViewById(R.id.tvDeudaCliente)
        val etMonto: EditText = view.findViewById(R.id.etMontoCobro)
        val btnCobrar: ImageButton = view.findViewById(R.id.btnCobrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente_cobro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = clientesConDeuda.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cliente = clientesConDeuda[position]
        var deudaActual = dbHelper.obtenerSaldoCliente(cliente.id)

        holder.tvNombre.text = cliente.nombre
        holder.tvDeuda.text = formato.format(deudaActual)

        formatoMilesPY(holder.etMonto)

        holder.btnCobrar.setOnClickListener {

            val montoStr = holder.etMonto.text.toString()

            if (montoStr.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Ingrese un monto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = montoStr.replace(".", "").toDoubleOrNull()

            if (monto == null) {
                Toast.makeText(holder.itemView.context, "Monto inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (monto > deudaActual) {
                Toast.makeText(holder.itemView.context, "El cobro no puede ser mayor a la deuda", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val idDeuda = obtenerUltimaDeudaId(cliente.id)
            val idCliente = dbHelper.obtenerClienteIdPorDeuda(idDeuda)

            val nuevoSaldo = deudaActual - monto

            val valoresCobro = ContentValues().apply {
                put("id_deuda", idDeuda)
                put("cli_id", idCliente)
                put("cob_fecha", System.currentTimeMillis().toString())
                put("monto", monto)
                put("saldo", nuevoSaldo)
            }

            db.insert("cobro", null, valoresCobro)

            val valoresDeuda = ContentValues().apply {
                put("totalDeuda", nuevoSaldo)
            }

            db.update("deuda", valoresDeuda, "id = ?", arrayOf(idDeuda.toString()))
            db.close()

            holder.etMonto.text.clear()
            holder.tvDeuda.text = formato.format(nuevoSaldo)

            Toast.makeText(holder.itemView.context, "Cobro registrado", Toast.LENGTH_SHORT).show()

            // 🔥 eliminar si ya no debe
            if (nuevoSaldo <= 0) {
                clientesConDeuda.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    // 🔎 FILTRO
    fun filtrar(texto: String) {
        clientesConDeuda.clear()

        val filtrados = listaOriginal.filter {
            dbHelper.obtenerSaldoCliente(it.id) > 0 &&
                    it.nombre.contains(texto, ignoreCase = true)
        }

        clientesConDeuda.addAll(filtrados)
        notifyDataSetChanged()
    }

    private fun obtenerUltimaDeudaId(clienteId: Int): Int {
        val db = dbHelper.readableDatabase
        var idDeuda = 0

        val cursor = db.rawQuery(
            "SELECT id FROM deuda WHERE cli_id = ? ORDER BY id DESC LIMIT 1",
            arrayOf(clienteId.toString())
        )

        if (cursor.moveToFirst()) {
            idDeuda = cursor.getInt(0)
        }

        cursor.close()
        return idDeuda
    }

    private fun formatoMilesPY(editText: EditText) {
        val formatter = DecimalFormat("#,###").apply {
            decimalFormatSymbols = decimalFormatSymbols.apply {
                groupingSeparator = '.'
            }
        }

        var current = ""

        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != current) {

                    editText.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toLong()
                        val formatted = if (parsed >= 1000) formatter.format(parsed) else parsed.toString()

                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } else {
                        current = ""
                    }

                    editText.addTextChangedListener(this)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}