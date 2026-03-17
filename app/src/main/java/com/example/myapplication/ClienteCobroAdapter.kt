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

    // Solo clientes con deuda > 0
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

        holder.btnCobrar.setOnClickListener {

            val montoStr = holder.etMonto.text.toString()

            if (montoStr.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Ingrese un monto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull()

            if (monto == null || monto <= 0) {
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

            if (idDeuda == 0) {
                Toast.makeText(holder.itemView.context, "Error al obtener deuda", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Calcular nuevo saldo
            val nuevoSaldo = deudaActual - monto

            // ✅ Insertar cobro con saldo
            val valoresCobro = ContentValues().apply {
                put("id_deuda", idDeuda)
                put("cli_id", idCliente)
                put("cob_fecha", System.currentTimeMillis().toString())
                put("monto", monto)
                put("saldo", nuevoSaldo)
            }

            val nuevoCobroId = db.insert("cobro", null, valoresCobro)
            Log.d("ClienteCobroAdapter", "Cobro insertado con id=$nuevoCobroId")

            // ✅ Actualizar deuda
            val valoresDeuda = ContentValues().apply {
                put("totalDeuda", nuevoSaldo)
            }

            db.update("deuda", valoresDeuda, "id = ?", arrayOf(idDeuda.toString()))

            db.close()

            // UI
            holder.etMonto.text.clear()
            holder.tvDeuda.text = formato.format(nuevoSaldo)

            Toast.makeText(holder.itemView.context, "Cobro registrado correctamente", Toast.LENGTH_SHORT).show()

            // 🔥 Si ya no debe, eliminar de la lista
            if (nuevoSaldo <= 0) {
                clientesConDeuda.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, clientesConDeuda.size)
            }
        }
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
}