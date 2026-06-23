package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast

object CSVPedidos {

    fun generarBytes(lista: List<Pedido>): ByteArray {
        val csv = buildString {
            appendLine("Cliente,Cantidad,Kilos,Precio")
            lista.forEach { pedido ->
                appendLine("${pedido.nom},${pedido.cantidad},%.0f,%.0f".format(pedido.kilos, pedido.precio))
            }
        }
        return csv.toByteArray()
    }

    fun crearCSVEnDescargas(context: Context, lista: List<Pedido>) {
        val fileName = "Pedidos_${System.currentTimeMillis()}.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(generarBytes(lista))
                }
                Toast.makeText(context, "CSV guardado en Descargas", Toast.LENGTH_SHORT).show()
            }
        } else {
            // API 26-28
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(dir, fileName)
            file.writeBytes(generarBytes(lista))
            Toast.makeText(context, "CSV guardado en Descargas", Toast.LENGTH_SHORT).show()
        }
    }
}