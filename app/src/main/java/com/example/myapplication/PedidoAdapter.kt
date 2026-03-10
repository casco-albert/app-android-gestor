package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import android.widget.CheckBox
import android.graphics.Color
class PedidoAdapter(
    private var lista: MutableList<Pedido>,
    private val db: SQLite,
    private val onEditarClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

    private val formato = DecimalFormat("#,###.0")
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNro: TextView = view.findViewById(R.id.editNroPedido)
        val txtClienteId: TextView = view.findViewById(R.id.editClienteId)
        val txtCantidad: TextView = view.findViewById(R.id.editCantidad)
        val txtKilo: TextView = view.findViewById(R.id.editKilos)
        val txtPrecio: TextView = view.findViewById(R.id.editPrecio)
        val checkEntrega: CheckBox = view.findViewById(R.id.checkEntrega)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pedido = lista[position]

        val fila = String.format(
            "%-5s %-7s %-7s %-5s %-2s %-2s",
            pedido.nroPedido,
            pedido.cliente,
            pedido.cantidad,
            formato.format(pedido.kilos),
            formato.format(pedido.precio),
            if (pedido.entrega == 1) "OK" else ""
        )

        holder.txtNro.text = fila

        holder.txtClienteId.visibility = View.GONE
        holder.txtCantidad.visibility = View.GONE
        holder.txtKilo.visibility = View.GONE
        holder.txtPrecio.visibility = View.GONE

        // Estado del checkbox
        holder.checkEntrega.isChecked = pedido.entrega == 1

        // Si está entregado y no tiene deuda, generar deuda
        if (pedido.entrega == 1 && !db.existeDeuda(pedido.id)) {

            db.generarDeuda(pedido.id, pedido.precio)

            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")) // verde claro

        } else {

            holder.itemView.setBackgroundColor(Color.TRANSPARENT)

        }
        holder.btnEditar.setOnClickListener {
            onEditarClick(pedido)
        }
    }
    // Método para actualizar la lista sin recrear el adapter
    fun actualizarLista(nuevaLista: MutableList<Pedido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}