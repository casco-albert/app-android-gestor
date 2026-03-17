package com.example.myapplication

data class Cobro(
    val id: Int,
    val idDeuda: Int,
    val idCliente: Int,
    val nombreCliente: String,
    val fecha: String,
    val monto: Double,
    val saldo: Double
)