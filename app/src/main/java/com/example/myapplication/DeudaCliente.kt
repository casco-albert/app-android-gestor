package com.example.myapplication

data class DeudaCliente(
    val cliente: String,
    val deuFecha: String,
    val deuCantidad: Int,
    val saldoAnterior: Double,
    val montoCobro: Double,
    val totalDeuda: Double
) {
    val deudaPendiente: Double
        get() = totalDeuda - montoCobro
}