package com.example.myapplication

import com.example.myapplication.ui.ApiService

data class PedidoRequest(
    val cli_id: Int,
    val id_carga: Int,
    val cantidad: Int
)