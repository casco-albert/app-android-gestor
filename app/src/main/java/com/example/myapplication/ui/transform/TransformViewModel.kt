package com.example.myapplication.ui.transform

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Cliente
import com.example.myapplication.databinding.ItemTransformBinding

class ClienteAdapter(
    private val onEditar: (Cliente) -> Unit,
    private val onEliminar: (Cliente) -> Unit
) : ListAdapter<Cliente, ClienteAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cliente = getItem(position)
        holder.binding.textNombre?.text = cliente.nombre
        holder.binding.textPrecio?.text = "Precio/Kilo: ${cliente.precioKilo} GS"

        holder.binding.btnEditar?.setOnClickListener { onEditar(cliente) }
        holder.binding.btnEliminar?.setOnClickListener { onEliminar(cliente) }
    }
}