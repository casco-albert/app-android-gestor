package com.example.myapplication

data class Pedido(
    val id: Int,
    val nro_pedido: String,
    val cli_id: Int,
    val id_carga: Int,
    val nom: String,
    var cantidad: Int,
    val kilos: Double,
    val precio: Double,
    var entrega: Int
)