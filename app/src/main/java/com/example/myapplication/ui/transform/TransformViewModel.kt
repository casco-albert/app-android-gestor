package com.example.myapplication.ui.transform

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Cliente
import com.example.myapplication.ClienteConPedido
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTransformBinding

class ClienteAdapter(
    private val onEditar: (Cliente) -> Unit,
    private val onEliminar: (Cliente) -> Unit,
    private val onClick: (ClienteConPedido) -> Unit
) : ListAdapter<ClienteConPedido, ClienteAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<ClienteConPedido>() {
        override fun areItemsTheSame(oldItem: ClienteConPedido, newItem: ClienteConPedido): Boolean {
            return oldItem.cliente.id == newItem.cliente.id
        }

        override fun areContentsTheSame(oldItem: ClienteConPedido, newItem: ClienteConPedido): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransformBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)
        val cliente = item.cliente

        // ✅ CHECK SEGÚN PEDIDO (SIEMPRE SETEAR)
        holder.binding.checkPedido?.isChecked = item.tienePedido

        // 🔒 Evitar que el usuario lo cambie manualmente
        holder.binding.checkPedido?.isEnabled = false

        // 🔢 MOSTRAR CANTIDAD REAL
        holder.binding.textCantidadPedido?.text = item.cantidadPedidos.toString()

        // 🎨 BACKGROUND SEGÚN ESTADO
        holder.binding.root.setBackgroundResource(
            if (item.tienePedido)
                R.drawable.bg_cliente_activo
            else
                R.drawable.bg_cliente
        )

        holder.binding.textNombre?.text = cliente.nom

        // 💰 PRECIO
        holder.binding.textPrecio?.text =
            "Precio/Kilo: ${cliente.preciokilo} GS"

        // 🧹 LIMPIAR LISTENERS (evita bugs por reciclado)
        holder.binding.btnEditar?.setOnClickListener(null)
        holder.binding.btnEliminar?.setOnClickListener(null)
        holder.binding.root.setOnClickListener(null)

        // 🎯 EVENTOS
        holder.binding.btnEditar?.setOnClickListener {
            onEditar(cliente)
        }

        holder.binding.btnEliminar?.setOnClickListener {
            onEliminar(cliente)
        }

        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }
}