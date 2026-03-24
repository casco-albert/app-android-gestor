package com.example.myapplication
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
class DeudaAdapter(
    private val context: Context,
    private val lista: List<Deuda>
) : ArrayAdapter<Deuda>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_deuda_cliente, parent, false)

        val item = lista[position]

        view.findViewById<TextView>(R.id.deuCliente).text = item.nombreCliente
        view.findViewById<TextView>(R.id.deuMonto).text = item.actual
        view.findViewById<TextView>(R.id.deuSaldoAnterior).text = item.monto
        view.findViewById<TextView>(R.id.deuTotalDeuda).text = item.saldo // o fecha si tenés

        return view
    }
}