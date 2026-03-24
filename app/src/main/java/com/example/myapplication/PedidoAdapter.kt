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

        // Asignar datos a cada columna
        holder.txtNro.text = pedido.nroPedido.toString()
        holder.txtClienteId.text = pedido.cliente
        holder.txtCantidad.text = pedido.cantidad.toString()
        holder.txtKilo.text = formato.format(pedido.kilos)
        holder.txtPrecio.text = formato.format(pedido.precio)

        // Evitar problemas con el listener al reciclar vistas
        holder.checkEntrega.setOnCheckedChangeListener(null)

        holder.checkEntrega.isChecked = pedido.entrega == 1

        // Estado visual
        if (pedido.entrega == 1) {
            holder.btnEditar.isEnabled = false
            holder.checkEntrega.isEnabled = false
            holder.btnEditar.alpha = 0.3f
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))
        } else {
            holder.btnEditar.isEnabled = true
            holder.checkEntrega.isEnabled = true
            holder.btnEditar.alpha = 1f
        }

        // Alternar color de filas (zebra)
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }

        // Evento checkbox
        holder.checkEntrega.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                db.marcarPedidoEntregado(pedido.id)

                db.generarDeuda(
                    pedido.id,
                    pedido.cli_id,
                    pedido.precio
                )

                Toast.makeText(
                    holder.itemView.context,
                    "Pedido entregado y deuda generada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))

            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        // Editar
        holder.btnEditar.setOnClickListener {
            onEditarClick(pedido)
        }
    }

    fun ordenarPorNumero() {
        lista.sortBy { it.nroPedido.toInt() }
        notifyDataSetChanged()
    }

    // 🔽 Orden descendente
    fun ordenarPorNumeroDesc() {
        lista.sortByDescending { it.nroPedido.toInt() }
        notifyDataSetChanged()
    }

    // 🔄 Actualizar lista
    fun actualizarLista(nuevaLista: MutableList<Pedido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

}