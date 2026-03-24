package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object ExportCSVPedidos {

    fun crearCSVEnDescargas(context: Context, lista: List<Pedido>) {
        val symbols = DecimalFormatSymbols(Locale("es", "PY")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formato = DecimalFormat("#,###.0", symbols)

        // Carpeta Descargas
        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpeta = File(baseDir, "PedidosCSV")
        if (!carpeta.exists()) carpeta.mkdirs()

        val file = File(carpeta, "Pedidos_${System.currentTimeMillis()}.csv")

        try {
            val writer = FileWriter(file)

            // Encabezado
            writer.append("Nro,Cliente,Cantidad,Kilos,Total\n")

            writer.append("Nro;Cliente;Cantidad;Kilos;Total\n")

            for (p in lista) {
                writer.append(
                    "${p.nroPedido};" +
                            "${p.cliente};" +
                            "${p.cantidad};" +
                            "${formato.format(p.kilos)};" +
                            "${formato.format(p.precio)}\n"
                )
            }

            writer.flush()
            writer.close()

            Toast.makeText(context, "CSV guardado en Descargas", Toast.LENGTH_SHORT).show()

            // 🔹 Compartir
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir CSV"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar CSV", Toast.LENGTH_SHORT).show()
        }
    }
}