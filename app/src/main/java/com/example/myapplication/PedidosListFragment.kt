package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentPedidosListBinding
import com.example.myapplication.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidosListFragment : Fragment() {

    private var _binding: FragmentPedidosListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PedidoAdapter
    private var idCargaSeleccionada: Int = -1

    private var listaCargas = listOf<Carga>()
    private var listaPedidos = mutableListOf<Pedido>()

    lateinit var txtTotalCantidad: TextView
    lateinit var txtTotalKilos: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         adapter = PedidoAdapter(
            mutableListOf(),
            onEditarClick = { pedido ->
                mostrarDialogEditarCantidad(pedido)
            },
            api = RetrofitClient.api
        )

        _binding = FragmentPedidosListBinding.inflate(inflater, container, false)

        binding.recyclerPedidos.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerPedidos.adapter = adapter
        val view = binding.root

        txtTotalCantidad = binding.txtTotalCantidad
        txtTotalKilos = binding.txtTotalKilos

        cargarSpinnerCargas()
        binding.btnPDF.setOnClickListener { compartirArchivo("pdf") }
        binding.btnCSV.setOnClickListener { compartirArchivo("csv") }

        return binding.root
    }

    // 🔹 SPINNER (cargas desde SQLite como ya tienes)
    private fun cargarSpinnerCargas() {

        RetrofitClient.api.obtenerCargas()
            .enqueue(object : Callback<ApiResponse<List<Carga>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<Carga>>>,
                    response: Response<ApiResponse<List<Carga>>>
                ) {

                    if (response.isSuccessful) {

                        listaCargas = response.body()?.data ?: emptyList()

                        val adapterSpinner = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listaCargas.map { it.descripcion }
                        )

                        adapterSpinner.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                        )

                        binding.spinnerCargas.adapter = adapterSpinner
                        binding.spinnerCargas.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {

                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                    val carga = listaCargas[position]
                                    idCargaSeleccionada = carga.id

                                    cargarPedidosDesdeAPI(idCargaSeleccionada)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }

                        // 🔥 AUTO CARGA INICIAL
                        if (listaCargas.isNotEmpty()) {
                            val primera = listaCargas[0]
                            idCargaSeleccionada = primera.id
                            cargarPedidosDesdeAPI(primera.id)

                            binding.spinnerCargas.setSelection(0)
                        }

                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Carga>>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Error cargas: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // 🔥 CARGAR PEDIDOS DESDE API REST
    private fun cargarPedidosDesdeAPI(idCarga: Int) {
        RetrofitClient.api.getPedidosPorCarga(idCarga)
            .enqueue(object : Callback<ApiResponse<List<Pedido>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Pedido>>>,
                    response: Response<ApiResponse<List<Pedido>>>
                ) {
                    if (response.isSuccessful) {
                        listaPedidos = response.body()?.data?.toMutableList() ?: mutableListOf()
                        adapter.actualizarLista(listaPedidos)
                        actualizarTotales(listaPedidos)
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Pedido>>>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
    // 🔹 EDITAR CANTIDAD (solo UI por ahora)
    private fun mostrarDialogEditarCantidad(pedido: Pedido) {

        val editText = EditText(requireContext())
        editText.setText(pedido.cantidad.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Cantidad")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->

                val nuevaCantidad = editText.text.toString().toIntOrNull()

                if (nuevaCantidad != null) {
                    Toast.makeText(
                        requireContext(),
                        "Implementa update por API aquí",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔹 TOTALES
    private fun actualizarTotales(lista: List<Pedido>) {

        val totalCantidad = lista.sumOf { it.cantidad }
        val totalKilos = lista.sumOf { it.kilos }

        txtTotalCantidad.text = "Cant: $totalCantidad"
        txtTotalKilos.text = "Kg: %.1f".format(totalKilos)
    }
    private fun PDFPedidos() {
        if (listaPedidos.isEmpty()) {
            Toast.makeText(requireContext(), "No hay pedidos para exportar", Toast.LENGTH_SHORT).show()
            return
        }
        PDFPedidos.crearPDFEnDescargas(requireContext(), listaPedidos)
    }

    private fun CSVPedidos() {
        if (listaPedidos.isEmpty()) {
            Toast.makeText(requireContext(), "No hay pedidos para exportar", Toast.LENGTH_SHORT).show()
            return
        }
        CSVPedidos.crearCSVEnDescargas(requireContext(), listaPedidos)
    }

    private fun compartirArchivo(tipo: String) {
        if (listaPedidos.isEmpty()) {
            Toast.makeText(requireContext(), "No hay pedidos para compartir", Toast.LENGTH_SHORT).show()
            return
        }

        val cargaNombre = listaCargas.firstOrNull { it.id == idCargaSeleccionada }?.descripcion ?: "carga"
        val fileName: String
        val mimeType: String
        val contenido: ByteArray

        if (tipo == "pdf") {
            fileName = "Pedidos_$cargaNombre.pdf"
            mimeType = "application/pdf"
            contenido = PDFPedidos.generarBytes(requireContext(), listaPedidos)
        } else {
            fileName = "Pedidos_$cargaNombre.csv"
            mimeType = "text/csv"
            contenido = CSVPedidos.generarBytes(listaPedidos)
        }

        // Guardar en caché
        val file = java.io.File(requireContext().cacheDir, fileName)
        file.writeBytes(contenido)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        startActivity(
            android.content.Intent.createChooser(
                android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Compartir pedidos"
            )
        )
    }


}