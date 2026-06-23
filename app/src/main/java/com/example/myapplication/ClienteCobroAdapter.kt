package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.Locale

class ClienteCobroAdapter(
    clientes: List<Cliente>
) : RecyclerView.Adapter<ClienteCobroAdapter.ViewHolder>() {

    private val listaOriginal    = clientes.toMutableList()
    private val clientesConDeuda = clientes.toMutableList()
    private val saldos           = mutableMapOf<Int, Double>()
    private val expandidos       = mutableSetOf<Int>()
    private val formato          = DecimalFormat.getNumberInstance(Locale("es", "PY"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre     : TextView     = view.findViewById(R.id.tvNombreCliente)
        val tvDeuda      : TextView     = view.findViewById(R.id.tvDeudaCliente)
        val ivFlecha     : ImageView    = view.findViewById(R.id.ivFlecha)
        val layoutHeader : LinearLayout = view.findViewById(R.id.layoutClienteHeader)
        val layoutCobro  : LinearLayout = view.findViewById(R.id.layoutCobro)
        val etMonto      : EditText     = view.findViewById(R.id.etMontoCobro)
        val btnCobrar    : ImageButton  = view.findViewById(R.id.btnCobrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente_cobro, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = clientesConDeuda.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cliente       = clientesConDeuda[position]
        val estaExpandido = expandidos.contains(cliente.id)

        // ── Nombre ─────────────────────────────────────────────
        holder.tvNombre.text = cliente.nom

        // ── Saldo (cache o API) ────────────────────────────────
        val saldoCacheado = saldos[cliente.id]
        if (saldoCacheado != null) {
            mostrarSaldo(holder, saldoCacheado)
        } else {
            holder.tvDeuda.text = "Cargando..."
            cargarSaldo(cliente.id) { saldo ->
                saldos[cliente.id] = saldo
                mostrarSaldo(holder, saldo)
                ordenarPorSaldo()
            }
        }

        // ── Accordion ──────────────────────────────────────────
        holder.layoutCobro.visibility = if (estaExpandido) View.VISIBLE else View.GONE
        holder.ivFlecha.rotation      = if (estaExpandido) 180f else 0f

        holder.layoutHeader.setOnClickListener {
            if (expandidos.contains(cliente.id)) {
                expandidos.remove(cliente.id)
            } else {
                expandidos.add(cliente.id)
                holder.etMonto.setText("")
            }
            notifyItemChanged(position)
        }

        // ── Formato miles paraguayo ────────────────────────────
        formatoMilesPY(holder.etMonto)

        // ── Botón cobrar ───────────────────────────────────────
        holder.btnCobrar.setOnClickListener {
            val montoStr = holder.etMonto.text.toString().trim()

            if (montoStr.isEmpty()) {
                holder.etMonto.error = "Ingresá un monto"
                return@setOnClickListener
            }

            val monto = montoStr.replace(".", "").toDoubleOrNull()
            if (monto == null || monto <= 0) {
                holder.etMonto.error = "Monto inválido"
                return@setOnClickListener
            }

            val deudaActual = saldos[cliente.id] ?: 0.0
            if (monto > deudaActual) {
                holder.etMonto.error = "Supera la deuda pendiente"
                return@setOnClickListener
            }

            mostrarDialogConfirmacion(holder, cliente, monto, position, deudaActual)
        }
    }

    // ── Ordenar por saldo descendente ─────────────────────────

    private fun ordenarPorSaldo() {
        clientesConDeuda.sortByDescending { saldos[it.id] ?: 0.0 }
        notifyDataSetChanged()
    }

    // ── Mostrar saldo con color ────────────────────────────────

    private fun mostrarSaldo(holder: ViewHolder, saldo: Double) {
        holder.tvDeuda.text = "Gs. ${formato.format(saldo)}"
        holder.tvDeuda.setTextColor(
            when {
                saldo <= 0     -> Color.parseColor("#2E7D32")
                saldo > 999000 -> Color.parseColor("#D32F2F")
                else           -> Color.parseColor("#E65100")
            }
        )
    }

    // ── Dialog de confirmación ─────────────────────────────────

    private fun mostrarDialogConfirmacion(
        holder      : ViewHolder,
        cliente     : Cliente,
        monto       : Double,
        position    : Int,
        deudaActual : Double
    ) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle("Confirmar cobro")
            .setMessage(
                "Cliente: ${cliente.nom}\n" +
                        "Monto a cobrar: Gs. ${formato.format(monto)}\n" +
                        "Deuda pendiente: Gs. ${formato.format(deudaActual)}\n\n" +
                        "¿Confirmás el cobro?"
            )
            .setPositiveButton("Confirmar") { _, _ ->
                registrarCobro(cliente.id, monto, position, holder, deudaActual)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── API: cargar saldo ──────────────────────────────────────

    private fun cargarSaldo(cliId: Int, onResult: (Double) -> Unit) {
        RetrofitClient.api
            .getSaldoCliente(cliId)
            .enqueue(object : Callback<SaldoResponse> {
                override fun onResponse(
                    call: Call<SaldoResponse>,
                    response: Response<SaldoResponse>
                ) {
                    onResult(response.body()?.saldo ?: 0.0)
                }
                override fun onFailure(call: Call<SaldoResponse>, t: Throwable) {
                    onResult(0.0)
                }
            })
    }

    // ── API: registrar cobro ───────────────────────────────────

    private fun registrarCobro(
        cliId       : Int,
        monto       : Double,
        position    : Int,
        holder      : ViewHolder,
        deudaActual : Double
    ) {
        RetrofitClient.api
            .registrarCobro(CobroRequest(cli_id = cliId, monto = monto))
            .enqueue(object : Callback<ApiResponse<CobroResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<CobroResponse>>,
                    response: Response<ApiResponse<CobroResponse>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        Toast.makeText(
                            holder.itemView.context,
                            "Cobro registrado ✓",
                            Toast.LENGTH_SHORT
                        ).show()

                        holder.etMonto.setText("")
                        expandidos.remove(cliId)

                        // Invalidar caché y recargar saldo real desde la API
                        saldos.remove(cliId)
                        holder.tvDeuda.text = "Actualizando..."

                        cargarSaldo(cliId) { nuevoSaldo ->
                            saldos[cliId] = nuevoSaldo
                            mostrarSaldo(holder, nuevoSaldo)

                            val idx = clientesConDeuda.indexOfFirst { it.id == cliId }
                            if (idx != -1) {
                                if (nuevoSaldo <= 0) {
                                    clientesConDeuda.removeAt(idx)
                                    notifyItemRemoved(idx)
                                } else {
                                    ordenarPorSaldo()
                                }
                            }
                        }

                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Error al registrar cobro",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CobroResponse>>, t: Throwable) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Sin conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ── Filtro de búsqueda ─────────────────────────────────────

    fun filtrar(texto: String) {
        clientesConDeuda.clear()
        clientesConDeuda.addAll(
            listaOriginal.filter { it.nom.contains(texto, ignoreCase = true) }
        )
        ordenarPorSaldo()
    }

    // ── Formato miles paraguayo ────────────────────────────────

    private fun formatoMilesPY(editText: EditText) {
        val formatter = DecimalFormat("#,###").apply {
            decimalFormatSymbols = decimalFormatSymbols.apply {
                groupingSeparator = '.'
            }
        }
        var current = ""
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed    = cleanString.toLong()
                        val formatted = if (parsed >= 1000) formatter.format(parsed) else parsed.toString()
                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } else {
                        current = ""
                    }
                    editText.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
