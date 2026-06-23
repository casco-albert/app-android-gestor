package com.example.myapplication

data class CobroResponse(
    val id: Int,
    val cli_id: Int,
    val id_deuda: Int,
    val monto: Double,
    val saldo: Double
)