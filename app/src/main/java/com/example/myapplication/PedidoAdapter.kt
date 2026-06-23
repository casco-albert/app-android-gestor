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
import com.example.myapplication.ui.ApiService
import java.text.DecimalFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoAdapter(
    private var lista: MutableList<Pedido>,
    private val onEditarClick: (Pedido) -> Unit,
    private val api: ApiService
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

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

        holder.txtNro.text = pedido.nro_pedido ?: ""
        holder.txtClienteId.text = pedido.nom ?: "N/A"
        holder.txtCantidad.text = pedido.cantidad.toString()
        holder.txtKilo.text = formato.format(pedido.kilos)
        holder.txtPrecio.text = formato.format(pedido.precio)

        holder.checkEntrega.setOnCheckedChangeListener(null)
        holder.checkEntrega.isChecked = pedido.entrega == 1

        if (pedido.entrega == 1) {
            holder.btnEditar.alpha = 0.3f
            holder.btnEditar.isEnabled = false
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))
        } else {
            holder.btnEditar.alpha = 1f
            holder.btnEditar.isEnabled = true

            if (position % 2 == 0)
                holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
            else
                holder.itemView.setBackgroundColor(Color.WHITE)
        }

        holder.checkEntrega.setOnCheckedChangeListener { _, isChecked ->

            val currentPos = holder.adapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener

            val pedidoActual = lista[currentPos]

            if (isChecked) {

                api.marcarEntregado(pedidoActual.id)
                    .enqueue(object : Callback<Void> {

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                            api.generarDeuda(pedidoActual.id)
                                .enqueue(object : Callback<Void> {

                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        pedidoActual.entrega = 1
                                        notifyItemChanged(currentPos)

                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Pedido entregado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                                })
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(holder.itemView.context, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })

            } else {

                api.eliminarDeuda(pedidoActual.id)
                    .enqueue(object : Callback<Void> {

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                            api.desmarcarEntregado(pedidoActual.id)
                                .enqueue(object : Callback<Void> {

                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        pedidoActual.entrega = 0
                                        notifyItemChanged(currentPos)

                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Deuda eliminada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                                })
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {}
                    })
            }
        }

        holder.btnEditar.setOnClickListener {
            onEditarClick(pedido)
        }
    }

    fun actualizarLista(nuevaLista: MutableList<Pedido>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
    fun ordenarPorNumero() {
        lista.sortBy { it.nro_pedido.toInt() }
        notifyDataSetChanged()
    }

    fun ordenarPorNumeroDesc() {
        lista.sortByDescending { it.nro_pedido.toInt() }
        notifyDataSetChanged()
    }
}