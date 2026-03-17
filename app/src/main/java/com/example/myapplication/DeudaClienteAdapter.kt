package com.example.myapplication
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeudaClienteAdapter(
    private val lista: MutableList<DeudaCliente>
) : RecyclerView.Adapter<DeudaClienteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCliente: TextView = view.findViewById(R.id.deuCliente)
        val txtFecha: TextView = view.findViewById(R.id.deuFecha)
        val txtMonto: TextView = view.findViewById(R.id.deuMonto)
        val txtSaldoAnterior: TextView = view.findViewById(R.id.deuSaldoAnterior)
        val txtTotalDeuda: TextView = view.findViewById(R.id.deuTotalDeuda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deuda, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deuda = lista[position]

        holder.txtCliente.text = deuda.cliente
        holder.txtFecha.text = deuda.deuFecha
        holder.txtMonto.text = deuda.monto.toString()
        holder.txtSaldoAnterior.text = deuda.saldoAnterior.toString()
        holder.txtTotalDeuda.text = deuda.totalDeuda.toString()
    }
}