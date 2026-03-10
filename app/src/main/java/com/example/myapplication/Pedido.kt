package com.example.myapplication

data class Pedido(
    val id: Int,
    val nroPedido: String,
    val cliente: String,
    var cantidad: Int,
    val kilos: Double,
    val precio: Double,
    val entrega: Int
)