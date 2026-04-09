package com.example.myapplication

data class Pedido(
    val id: Int,
    val nroPedido: String,
    val cli_id: Int,
    val cliente: String,
    var cantidad: Int,
    val kilos: Double,
    val precio: Double,
    var entrega: Int
)