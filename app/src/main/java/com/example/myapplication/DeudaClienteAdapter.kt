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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DeudaClienteAdapter(
    context: Context,
    private val lista: List<DeudaCliente>
) : ArrayAdapter<DeudaCliente>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_deuda, parent, false)

        val deuda = lista[position]

        val tvCliente       = view.findViewById<TextView>(R.id.deuCliente)
        val tvFecha         = view.findViewById<TextView>(R.id.deuFecha)
        val tvCantidad      = view.findViewById<TextView>(R.id.deuMonto)
        val tvSaldoAnterior = view.findViewById<TextView>(R.id.deuSaldoAnterior)
        val tvPago          = view.findViewById<TextView>(R.id.deuPago)
        val tvTotal         = view.findViewById<TextView>(R.id.deuTotalDeuda)

        // ── Datos ──────────────────────────────────────────────
        tvCliente.text       = deuda.cliente
        tvFecha.text         = formatearFecha(deuda.deuFecha)
        tvCantidad.text      = deuda.deuCantidad.toString()
        tvSaldoAnterior.text = formatearNumero(deuda.saldoAnterior)
        tvPago.text          = formatearNumero(deuda.montoCobro)
        tvTotal.text         = formatearNumero(deuda.deudaPendiente) // ✅ usa propiedad calculada

        // ── Alineación tipo tabla ──────────────────────────────
        tvCliente.gravity       = Gravity.START
        tvFecha.gravity         = Gravity.CENTER
        tvCantidad.gravity      = Gravity.END
        tvSaldoAnterior.gravity = Gravity.END
        tvPago.gravity          = Gravity.END
        tvTotal.gravity         = Gravity.END

        // ── Fuente monospace ───────────────────────────────────
        tvCantidad.typeface      = Typeface.MONOSPACE
        tvSaldoAnterior.typeface = Typeface.MONOSPACE
        tvPago.typeface          = Typeface.MONOSPACE
        tvTotal.typeface         = Typeface.MONOSPACE

        // ── Colores de texto ───────────────────────────────────
        tvSaldoAnterior.setTextColor(Color.parseColor("#D32F2F"))
        tvPago.setTextColor(Color.parseColor("#2E7D32"))

        // ── Color del total según deuda pendiente ──────────────
        tvTotal.setTextColor(
            when {
                deuda.deudaPendiente <= 0    -> Color.parseColor("#2E7D32") // verde — pagado
                deuda.deudaPendiente > 299000 -> Color.parseColor("#D32F2F") // rojo — deuda alta
                else                          -> Color.parseColor("#E65100") // naranja — deuda parcial
            }
        )

        // ── Color de fondo según deuda pendiente ───────────────
        view.setBackgroundColor(
            when {
                deuda.deudaPendiente <= 0     -> Color.parseColor("#E8F5E9") // verde suave — pagado
                deuda.deudaPendiente > 299000 -> Color.parseColor("#FFCDD2") // rojo suave — deuda alta
                else -> if (position % 2 == 0) Color.parseColor("#FFFFFF")
                else                   Color.parseColor("#F5F5F5")   // zebra
            }
        )

        return view
    }

    // ── Helpers ────────────────────────────────────────────────

    fun formatearNumero(numero: Double): String {
        return DecimalFormat("#,##0").format(numero)
    }

    fun formatearFecha(fecha: String): String {
        return try {
            if (fecha.all { it.isDigit() }) {
                val timestamp = fecha.toLong()
                SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp * 1000))
            } else {
                val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val salida  = SimpleDateFormat("dd/MM",      Locale.getDefault())
                salida.format(entrada.parse(fecha)!!)
            }
        } catch (e: Exception) {
            fecha
        }
    }
}