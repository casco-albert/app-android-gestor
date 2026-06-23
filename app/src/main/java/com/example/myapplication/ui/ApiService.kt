package com.example.myapplication.ui


import com.example.myapplication.ActualizarCantidadRequest
import com.example.myapplication.ApiResponse
import com.example.myapplication.Carga
import com.example.myapplication.Cliente
import com.example.myapplication.ClienteItem
import com.example.myapplication.CobroRequest
import com.example.myapplication.CobroResponse
import com.example.myapplication.DeudaClienteDTO
import com.example.myapplication.HistorialCobroDTO
import com.example.myapplication.Pedido
import com.example.myapplication.PedidoRequest
import com.example.myapplication.PedidoResponse
import com.example.myapplication.SaldoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("cargas")
    fun obtenerCargas(): Call<ApiResponse<List<Carga>>>

    @POST("cargas")
    fun insertarCarga(@Body carga: Carga): Call<ApiResponse<Carga>>

    @GET("cargas/ultima")
    fun obtenerUltimaCarga(): Call<ApiResponse<Carga>>


    // CLIENTES
    @GET("clientes")
    fun obtenerClientes(): Call<ApiResponse<List<Cliente>>>

    @DELETE("clientes/{id}")
    fun eliminarCliente(@Path("id") id: Int): Call<Void>

    @PUT("clientes/{id}")
    fun actualizarCliente(@Path("id") id: Int, @Body cliente: Cliente): Call<Cliente>
    @POST("clientes")
    fun insertarCliente(@Body cliente: Cliente): Call<Cliente>

    @GET("clientes/ultima")
    fun obtenerUltimoCliente(): Call<Cliente>
    ///PEDIDOS
    @GET("clientes")
    fun getClientes(): Call<ApiResponse<List<Cliente>>>

    @PUT("pedidos/{id}/cantidad")
    fun actualizarPedido(@Path("id") id: Int, @Body body: ActualizarCantidadRequest): Call<PedidoResponse>
    @GET("cargas/ultima")
    fun getUltimaCarga(): Call<Int?>

    @GET("pedidos/carga/{idCarga}")
    fun getPedidosPorCarga(@Path("idCarga") idCarga: Int): Call<ApiResponse<List<Pedido>>>
    @GET("pedidos")
    fun getPedidos(): Call<ApiResponse<List<Pedido>>>
    @POST("pedidos")
    fun crearPedido(@Body pedido: PedidoRequest): Call<PedidoResponse>
    // DESPUÉS (correcto según tu api.php)
    @PUT("pedidos/{id}/entregar")
    fun marcarEntregado(@Path("id") id: Int): Call<Void>

    @PUT("pedidos/{id}/desmarcar-entrega")
    fun desmarcarEntregado(@Path("id") id: Int): Call<Void>

    @POST("deudas/generar/{id}")
    fun generarDeuda(@Path("id") id: Int): Call<Void>

    @DELETE("deudas/{id}")
    fun eliminarDeuda(@Path("id") id: Int): Call<Void>

    //Deudas por cliente
    @GET("deudas")
    fun getDeudas(): Call<ApiResponse<List<DeudaClienteDTO>>>
    @GET("clientes/{id}/saldo")
    fun getSaldoCliente(@Path("id") id: Int): Call<SaldoResponse>

    @POST("cobros")
    fun registrarCobro(@Body body: CobroRequest): Call<ApiResponse<CobroResponse>>
    @GET("clientes/con-deuda")
    fun getClientesConDeuda(): Call<ApiResponse<List<Cliente>>>
    @GET("cobros")
    fun getCobros(): Call<ApiResponse<List<HistorialCobroDTO>>>
}

