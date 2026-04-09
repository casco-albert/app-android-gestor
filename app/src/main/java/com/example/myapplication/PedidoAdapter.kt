package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class PedidoAdapter(
    private var lista: MutableList<Pedido>,
    private val db: SQLite,
    private val onEditarClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

    // Formato sin decimales y con miles
    private val formato = DecimalFormat("#,###")

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNro: TextView = view.findViewById(R.id.editNroPedido)
        val txtClienteId: TextView = view.findViewById(R.id.editClienteId)
        val txtCantidad: TextView = view.findViewById(R.id.editCantidad)
        val txtKilo: TextView = view.findViewById(R.id.editKilos)
        val txtPrecio: TextView = view.findViewById(R.id.editPrecio)
        val checkEntrega: CheckBox = view.findViewById(R.id.check)
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

        // Asignar datos
        holder.txtNro.text = (position + 1).toString()
        holder.txtClienteId.text = pedido.cliente
        holder.txtCantidad.text = pedido.cantidad.toString()
        holder.txtKilo.text = formato.format(pedido.kilos)
        holder.txtPrecio.text = formato.format(pedido.precio)

        // 🔥 Evitar bug de reciclado
        holder.checkEntrega.setOnCheckedChangeListener(null)

        holder.checkEntrega.isChecked = pedido.entrega == 1

        if (pedido.entrega == 1) {
            //holder.btnEditar.isEnabled = false
            holder.btnEditar.alpha = 0.3f
            holder.checkEntrega.isEnabled = true

            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))
        } else {
            holder.btnEditar.isEnabled = true
            holder.btnEditar.alpha = 1f
            holder.checkEntrega.isEnabled = true

            // Zebra
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE)
            }
        }

        // ✅ Evento checkbox
        holder.checkEntrega.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                // Marcar entregado
                db.marcarPedidoEntregado(pedido.id)

                // Generar deuda
                db.generarDeuda(
                    pedido.id,
                    pedido.cli_id,
                    pedido.precio
                )

                // Actualizar modelo
                pedido.entrega = 1

                Toast.makeText(
                    holder.itemView.context,
                    "Pedido entregado y deuda generada",
                    Toast.LENGTH_SHORT
                ).show()

                holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))

            } else {

                // Eliminar deuda
                db.eliminarDeudaPorPedido(pedido.id)

                // Desmarcar entregado
                db.desmarcarPedidoEntregado(pedido.id)

                // Actualizar modelo
                pedido.entrega = 0

                Toast.makeText(
                    holder.itemView.context,
                    "Deuda eliminada",
                    Toast.LENGTH_SHORT
                ).show()

                // Restaurar zebra
                if (position % 2 == 0) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE)
                }
            }
        }

        // Editar
        holder.btnEditar.setOnClickListener {
            onEditarClick(pedido)
        }
    }

    fun ordenarPorNumero() {
        lista.sortBy {
            it.nroPedido.toDouble()
        }
        notifyDataSetChanged()
    }

    fun ordenarPorNumeroDesc() {
        lista.sortByDescending { it.nroPedido.toInt() }
        notifyDataSetChanged()
    }

    fun actualizarLista(nuevaLista: MutableList<Pedido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    fun eliminarItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}