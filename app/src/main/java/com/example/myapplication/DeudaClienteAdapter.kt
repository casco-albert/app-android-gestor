package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
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

        val cliente = view.findViewById<TextView>(R.id.deuCliente)
        val fecha = view.findViewById<TextView>(R.id.deuFecha)
        val monto = view.findViewById<TextView>(R.id.deuMonto)
        val saldoAnterior = view.findViewById<TextView>(R.id.deuSaldoAnterior)
        val pago = view.findViewById<TextView>(R.id.deuPago)
        val total = view.findViewById<TextView>(R.id.deuTotalDeuda)

        // 🔥 DATOS
        cliente.text = deuda.cliente
        fecha.text = formatearFecha(deuda.deuFecha)
        monto.text = formatearNumero(deuda.monto)
        saldoAnterior.text = formatearNumero(deuda.saldoAnterior)
        pago.text = formatearNumero(deuda.montoCobro)
        total.text = formatearNumero(deuda.totalDeuda)

        // 🔥 ALINEACIÓN TIPO TABLA (Excel)
        cliente.gravity = Gravity.START
        fecha.gravity = Gravity.CENTER
        monto.gravity = Gravity.END
        saldoAnterior.gravity = Gravity.END
        pago.gravity = Gravity.END
        total.gravity = Gravity.END

        // 🔥 FUENTE MONOSPACE (mejor alineación numérica)
        monto.typeface = Typeface.MONOSPACE
        saldoAnterior.typeface = Typeface.MONOSPACE
        pago.typeface = Typeface.MONOSPACE
        total.typeface = Typeface.MONOSPACE

        // 🔥 COLOR (opcional pro)
        saldoAnterior.setTextColor(Color.parseColor("#D32F2F"))
        pago.setTextColor(Color.parseColor("#2E7D32"))

        // 🔥 COLOR DE FONDO SEGÚN SALDO
        when {
            deuda.saldoAnterior > 299000 -> {
                view.setBackgroundColor(Color.parseColor("#FFCDD2")) // rojo suave
            }
            deuda.saldoAnterior == 0.0 -> {
                view.setBackgroundColor(Color.parseColor("#E8F5E9")) // verde suave
            }
            else -> {
                // efecto zebra (filas intercaladas)
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                } else {
                    view.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }            }
        }


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