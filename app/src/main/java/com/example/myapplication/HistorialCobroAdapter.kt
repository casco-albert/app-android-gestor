package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialCobroAdapter(
    private var lista: MutableList<HistorialCobro>
) : RecyclerView.Adapter<HistorialCobroAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCobro)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoCobro)
        val tvDeuda: TextView = view.findViewById(R.id.tvSaldo)
    }

    // 🔹 ACTUALIZAR LISTA (para filtro)
    fun actualizarLista(nuevaLista: List<HistorialCobro>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    // 🔥 NUEVO: obtener lista actual (filtrada)
    fun obtenerLista(): List<HistorialCobro> {
        return lista
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_cobro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.tvFecha.text = "${item.nombreCliente} - ${item.fecha}"

        val formato = java.text.DecimalFormat("#,###")
        holder.tvMonto.text = formato.format(item.monto)
        holder.tvDeuda.text = formato.format(item.saldo)

        // 🔴 Pintar saldo
        when {
            item.saldo > 100000 -> {
                holder.tvDeuda.setTextColor(android.graphics.Color.RED)
            }
            item.saldo == 0.0 -> {
                holder.tvDeuda.setTextColor(android.graphics.Color.parseColor("#2E7D32")) // verde suave
            }
            else -> {
                holder.tvDeuda.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}