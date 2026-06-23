package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.ui.ApiService
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoFragment : Fragment() {

    private lateinit var spinnerClientes: Spinner
    private lateinit var editCantidad: EditText
    private lateinit var editKilos: EditText
    private lateinit var editPrecio: EditText
    private lateinit var btnGuardar: Button

    private var cliId: Int = 0
    private var idCarga: Int = 0
    private var esEdicion: Boolean = false
    private var pedidoId: Int = 0
    private var cantidadActual: Int = 0

    private lateinit var api: ApiService
    private var listaClientes = mutableListOf<Cliente>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_pedidos, container, false)

        spinnerClientes = view.findViewById(R.id.spinnerClientes)
        editCantidad = view.findViewById(R.id.editCantidad)
        editKilos = view.findViewById(R.id.editKilos)
        editPrecio = view.findViewById(R.id.editPrecio)
        btnGuardar = view.findViewById(R.id.btnGuardarPedido)

        // Leer argumentos
        cliId = arguments?.getInt("cli_id", 0) ?: 0
        idCarga = arguments?.getInt("id_carga", 0) ?: 0
        esEdicion = arguments?.getBoolean("es_edicion", false) ?: false
        pedidoId = arguments?.getInt("pedido_id", 0) ?: 0
        cantidadActual = arguments?.getInt("cantidad_actual", 0) ?: 0

        api = RetrofitClient.api

        editKilos.isEnabled = false
        editPrecio.isEnabled = false

        // Configurar UI según modo
        if (esEdicion) {
            btnGuardar.text = "Actualizar"
            spinnerClientes.isEnabled = false
            editCantidad.setText(cantidadActual.toString())
        } else {
            btnGuardar.text = "Guardar"
        }

        cargarClientes()

        editCantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calcularTotales()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        spinnerClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calcularTotales()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnGuardar.setOnClickListener {
            if (esEdicion) actualizarPedido() else guardarPedido()
        }

        return view
    }

    private fun cargarClientes() {
        api.getClientes().enqueue(object : Callback<ApiResponse<List<Cliente>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Cliente>>>,
                response: Response<ApiResponse<List<Cliente>>>
            ) {
                if (!response.isSuccessful) {
                    Toast.makeText(requireContext(), "Error cargando clientes", Toast.LENGTH_SHORT).show()
                    return
                }

                listaClientes = response.body()?.data?.toMutableList() ?: mutableListOf()

                val nombres = listaClientes.map { it.nom }
                val adapterSpinner = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    nombres
                )
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClientes.adapter = adapterSpinner

                val posicion = listaClientes.indexOfFirst { it.id == cliId }
                if (posicion >= 0) spinnerClientes.setSelection(posicion)

                calcularTotales()
            }

            override fun onFailure(call: Call<ApiResponse<List<Cliente>>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun calcularTotales() {
        if (!this::spinnerClientes.isInitialized) return
        if (listaClientes.isEmpty()) return
        if (spinnerClientes.selectedItemPosition < 0) return

        val position = spinnerClientes.selectedItemPosition
        if (position >= listaClientes.size) return

        val cantidad = editCantidad.text.toString().toIntOrNull() ?: 0
        val cliente = listaClientes[position]

        val kilos = cantidad * 40.0
        val precio = kilos * cliente.preciokilo

        editKilos.setText("%.0f".format(kilos))
        editPrecio.setText("%.0f".format(precio))
    }

    private fun guardarPedido() {
        if (listaClientes.isEmpty()) {
            Toast.makeText(requireContext(), "No hay clientes disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidad = editCantidad.text.toString().toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            Toast.makeText(requireContext(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        val cliente = listaClientes.find { it.id == cliId }
        if (cliente == null) {
            Toast.makeText(requireContext(), "Cliente no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val pedido = PedidoRequest(
            cli_id = cliente.id,
            id_carga = idCarga,
            cantidad = cantidad
        )

        api.crearPedido(pedido).enqueue(object : Callback<PedidoResponse> {
            override fun onResponse(
                call: Call<PedidoResponse>,
                response: Response<PedidoResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Pedido guardado correctamente", Toast.LENGTH_SHORT).show()
                    editCantidad.setText("")
                    editKilos.setText("")
                    editPrecio.setText("")
                } else {
                    Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun actualizarPedido() {
        val cantidad = editCantidad.text.toString().toIntOrNull()

        if (cantidad == null || cantidad < 0) {
            Toast.makeText(requireContext(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        // Si cantidad es 0 → confirmar eliminación
        if (cantidad == 0) {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Pedido")
                .setMessage("¿Eliminar el pedido de este cliente?")
                .setPositiveButton("Sí") { _, _ ->
                    enviarActualizacion(0)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        enviarActualizacion(cantidad)
    }

    private fun enviarActualizacion(cantidad: Int) {
        api.actualizarPedido(pedidoId, ActualizarCantidadRequest(cantidad = cantidad))
            .enqueue(object : Callback<PedidoResponse> {
                override fun onResponse(
                    call: Call<PedidoResponse>,
                    response: Response<PedidoResponse>
                ) {
                    if (response.isSuccessful) {
                        val mensaje = if (cantidad == 0) "Pedido eliminado" else "Pedido actualizado correctamente"
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()  // volver al listado
                    } else {
                        Toast.makeText(requireContext(), "Error ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}