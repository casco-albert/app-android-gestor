package com.example.myapplication

object DeudaMapper {
    fun fromDTO(dto: DeudaClienteDTO) = DeudaCliente(
        cliente       = dto.nom,
        deuFecha      = dto.deu_fecha,
        deuCantidad   = dto.cantidad,
        saldoAnterior = dto.saldoAnterior,
        montoCobro    = dto.montoCobro,
        totalDeuda    = dto.totalDeuda
    )

}