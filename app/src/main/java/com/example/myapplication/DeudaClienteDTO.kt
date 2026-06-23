package com.example.myapplication

data class DeudaClienteDTO(
    val nom: String,
    val deu_fecha: String,
    val cantidad: Int,
    val saldoAnterior: Double,
    val montoCobro: Double,
    val totalDeuda: Double
)