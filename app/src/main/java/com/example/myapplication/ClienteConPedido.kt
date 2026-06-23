package com.example.myapplication

data class ClienteConPedido(
    val cliente: Cliente,
    val tienePedido: Boolean,
    val cantidadPedidos: Int,
    val pedidoId: Int = 0,
    val cantidadActual: Int = 0
)