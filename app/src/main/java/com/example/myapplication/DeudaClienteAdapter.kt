package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormat
import java.util.Locale
class DeudaClienteAdapter(
    context: Context,
    private val lista: List<DeudaCliente>
) : ArrayAdapter<DeudaCliente>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_deuda, parent, false)

        val deuda = lista[position]

        view.findViewById<TextView>(R.id.deuCliente).text = deuda.cliente
        view.findViewById<TextView>(R.id.deuFecha).text = formatearFecha(deuda.deuFecha)
        view.findViewById<TextView>(R.id.deuMonto).text = formatearNumero(deuda.monto)
        view.findViewById<TextView>(R.id.deuSaldoAnterior).text = formatearNumero(deuda.saldoAnterior)
        view.findViewById<TextView>(R.id.deuTotalDeuda).text = formatearNumero(deuda.totalDeuda)

        return view
    }
    fun formatearNumero(numero: Double): String {
        val formato = DecimalFormat("#,##0")
        return formato.format(numero)
    }
    fun formatearFecha(fecha: String): String {
        return try {
            // 🔹 Si es timestamp (número)
            if (fecha.all { it.isDigit() }) {
                val timestamp = fecha.toLong()
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formato.format(Date(timestamp))
            } else {
                // 🔹 Si viene como string tipo "2026-03-23"
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val date = formatoEntrada.parse(fecha)
                formatoSalida.format(date!!)
            }
        } catch (e: Exception) {
            fecha // si falla, devuelve lo que venga
        }
    }
}