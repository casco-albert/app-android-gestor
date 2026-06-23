package com.example.myapplication.ui.transform

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.ApiResponse
import com.example.myapplication.Carga
import com.example.myapplication.Cliente
import com.example.myapplication.ClienteConPedido
import com.example.myapplication.Pedido
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTransformBinding
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClienteAdapter
    private var listaOriginal: List<ClienteConPedido> = listOf()
    private val api = RetrofitClient.api

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransformBinding.inflate(inflater, container, false)

        adapter = ClienteAdapter(

            onEditar = { cliente ->
                mostrarDialogEditar(cliente)
            },

            onEliminar = { cliente ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Cliente")
                    .setMessage("¿Eliminar a ${cliente.nom}?")
                    .setPositiveButton("Sí") { dialog, _ ->
                        api.eliminarCliente(cliente.id)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(
                                    call: Call<Void>,
                                    response: Response<Void>
                                ) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Cliente eliminado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    cargarClientes()
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error: ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },

            onClick = { item ->
                if (item.tienePedido) {
                    // Cliente CON pedido → modo edición
                    val bundle = Bundle().apply {
                        putInt("cli_id", item.cliente.id)
                        putBoolean("es_edicion", true)
                        putInt("pedido_id", item.pedidoId)
                        putInt("cantidad_actual", item.cantidadActual)
                    }
                    findNavController().navigate(R.id.pedidoFragment, bundle)

                } else {
                    // Cliente SIN pedido → consultar última carga y crear
                    api.obtenerUltimaCarga().enqueue(object : Callback<ApiResponse<Carga>> {
                        override fun onResponse(
                            call: Call<ApiResponse<Carga>>,
                            response: Response<ApiResponse<Carga>>
                        ) {
                            val ultimaCarga = response.body()?.data
                            if (ultimaCarga == null) {
                                Toast.makeText(
                                    requireContext(),
                                    "No hay carga activa",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                            val bundle = Bundle().apply {
                                putInt("cli_id", item.cliente.id)
                                putInt("id_carga", ultimaCarga.id)
                                putBoolean("es_edicion", false)
                            }
                            findNavController().navigate(R.id.pedidoFragment, bundle)
                        }
                        override fun onFailure(call: Call<ApiResponse<Carga>>, t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener carga: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }
        )

        binding.recyclerviewTransform.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewTransform.adapter = adapter

        binding.editBuscarCliente.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filtrar(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cargarClientes()

        return binding.root
    }

    private fun cargarClientes() {
        api.obtenerClientes().enqueue(object : Callback<ApiResponse<List<Cliente>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Cliente>>>,
                response: Response<ApiResponse<List<Cliente>>>
            ) {
                if (!response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val clientes = response.body()?.data ?: emptyList()

                api.getPedidos().enqueue(object : Callback<ApiResponse<List<Pedido>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<List<Pedido>>>,
                        response: Response<ApiResponse<List<Pedido>>>
                    ) {
                        val pedidos = response.body()?.data ?: emptyList()

                        listaOriginal = clientes.map { cliente ->
                            val pedido = pedidos.firstOrNull { it.cli_id == cliente.id }
                            ClienteConPedido(
                                cliente = cliente,
                                tienePedido = pedido != null,
                                cantidadPedidos = pedido?.cantidad ?: 0,
                                pedidoId = pedido?.id ?: 0,
                                cantidadActual = pedido?.cantidad ?: 0
                            )
                        }

                        adapter.submitList(listaOriginal.toList())
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<List<Pedido>>>,
                        t: Throwable
                    ) {
                        // Si falla pedidos, mostrar clientes sin estado
                        listaOriginal = clientes.map { cliente ->
                            ClienteConPedido(
                                cliente = cliente,
                                tienePedido = false,
                                cantidadPedidos = 0
                            )
                        }
                        adapter.submitList(listaOriginal.toList())
                    }
                })
            }

            override fun onFailure(
                call: Call<ApiResponse<List<Cliente>>>,
                t: Throwable
            ) {
                android.util.Log.e("API_CLIENTES", "Error Retrofit", t)
                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun filtrar(texto: String) {
        val listaFiltrada = listaOriginal.filter {
            it.cliente.nom.contains(texto, ignoreCase = true)
        }
        adapter.submitList(listaFiltrada.toList())
    }

    private fun mostrarDialogEditar(cliente: Cliente) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_cliente, null)

        val inputRec = dialogView.findViewById<EditText>(R.id.editRec)
        val inputNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.editPrecio)

        inputRec.setText(cliente.rec.toString())
        inputNombre.setText(cliente.nom)
        inputPrecio.setText(cliente.preciokilo.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val actualizado = Cliente(
                    id = cliente.id,
                    rec = inputRec.text.toString().toDoubleOrNull() ?: cliente.rec,
                    nom = inputNombre.text.toString().ifBlank { cliente.nom },
                    direc = cliente.direc?.ifBlank { "" } ?: "",
                    telef = cliente.telef ?: "",
                    preciokilo = inputPrecio.text.toString().toDoubleOrNull() ?: cliente.preciokilo
                )
                api.actualizarCliente(cliente.id, actualizado)
                    .enqueue(object : Callback<Cliente> {
                        override fun onResponse(
                            call: Call<Cliente>,
                            response: Response<Cliente>
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "Cliente actualizado",
                                Toast.LENGTH_SHORT
                            ).show()
                            cargarClientes()
                        }
                        override fun onFailure(call: Call<Cliente>, t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        cargarClientes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}