package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistorialCobroMapper {

    private val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun fromDTO(dto: HistorialCobroDTO) = HistorialCobro(
        id            = dto.id,
        idDeuda       = dto.id_deuda,
        idCliente     = dto.cli_id,
        nombreCliente = dto.nombreCliente,
        fecha         = convertirFecha(dto.cob_fecha), // ✅ siempre llega como yyyy-MM-dd
        monto         = dto.monto,
        saldo         = dto.saldo
    )

    private fun convertirFecha(valor: String): String {
        return try {
            if (valor.all { it.isDigit() }) {
                // Es timestamp Unix en segundos → convertir a yyyy-MM-dd
                val ms = valor.toLong() * 1000
                formatoSalida.format(Date(ms))
            } else {
                valor // ya viene como string de fecha, no tocar
            }
        } catch (e: Exception) {
            valor
        }
    }
}