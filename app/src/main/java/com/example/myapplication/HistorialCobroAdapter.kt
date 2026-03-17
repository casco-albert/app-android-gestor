package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class HistorialCobroAdapter(
    private val lista: List<HistorialCobro>
) : RecyclerView.Adapter<HistorialCobroAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCobro)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoCobro)
        val tvDeuda: TextView = view.findViewById(R.id.tvSaldo)
        val btnPDF: ImageButton = view.findViewById(R.id.btnExportPDF)
        val btnExcel: ImageButton = view.findViewById(R.id.btnExportExcel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_cobro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // Mostrar nombre del cliente y fecha formateada
        holder.tvFecha.text = "${item.nombreCliente} - ${item.fecha}"
        holder.tvMonto.text = item.monto.toString()
        holder.tvDeuda.text = item.saldo.toString()

        holder.btnPDF.setOnClickListener {
            Toast.makeText(it.context, "Exportar PDF: ${item.id}", Toast.LENGTH_SHORT).show()
        }

        holder.btnExcel.setOnClickListener {
            Toast.makeText(it.context, "Exportar Excel: ${item.id}", Toast.LENGTH_SHORT).show()
        }
    }
}