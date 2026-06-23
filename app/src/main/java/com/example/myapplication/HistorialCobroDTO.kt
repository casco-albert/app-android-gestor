package com.example.myapplication

data class HistorialCobroDTO(
    val id: Int,
    val id_deuda: Int,
    val cli_id: Int,
    val nombreCliente: String,
    val cob_fecha: String,
    val monto: Double,
    val saldo: Double
)