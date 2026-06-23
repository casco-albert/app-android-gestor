package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.OutputStreamWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object ExportCSV {

    fun crearCSVEnCarpeta(context: Context, lista: List<HistorialCobro>) {
        val symbols = DecimalFormatSymbols(Locale("es", "PY")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formato = DecimalFormat("#,###.0", symbols)

        val fileName = "Historial_Cobros_${System.currentTimeMillis()}.csv"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri: Uri = resolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: run {
            Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            resolver.openOutputStream(uri)?.use { output ->
                val writer = OutputStreamWriter(output)

                writer.append("Cliente;Fecha;Monto;Saldo\n")

                for (item in lista) {
                    writer.append(
                        "${item.nombreCliente};" +
                                "${item.fecha};" +
                                "${formato.format(item.monto)};" +
                                "${formato.format(item.saldo)}\n"
                    )
                }

                writer.flush()
            }

            Toast.makeText(context, "CSV guardado en Descargas", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir CSV"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}