package com.example.myapplication

data class DeudaCliente(
    val cliente: String,
    val deuFecha: String,
    val monto: Double,
    val saldoAnterior: Double,
    val montoCobro: Double,
    val totalDeuda: Double
)