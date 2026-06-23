package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.text.DecimalFormat

object ExportPDF {

    fun crearPDFEnDescargas(context: Context, lista: List<HistorialCobro>) {

        val listaOrdenada = lista.sortedByDescending { it.fecha }
        val formato = DecimalFormat("#,##0")

        // ✅ MediaStore en vez de FileOutputStream directo
        val fileName = "Historial_Cobros_${System.currentTimeMillis()}.pdf"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri: Uri = resolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        ) ?: run {
            Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
            return
        }

        resolver.openOutputStream(uri)?.use { outputStream ->

            val document = PdfDocument()
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(595, 842, 1).create()
            )
            val canvas = page.canvas

            val paint = Paint().apply { textSize = 12f }
            val paintBold = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }

            val xFecha    = 40f
            val xCliente  = 120f
            val xMonto    = 340f
            val xSaldo    = 450f

            var y = 50f

            // Encabezados
            canvas.drawText("Fecha",    xFecha,   y, paintBold)
            canvas.drawText("Cliente",  xCliente, y, paintBold)
            canvas.drawText("Monto",    xMonto,   y, paintBold)
            canvas.drawText("Saldo",    xSaldo,   y, paintBold)

            y += 15f
            canvas.drawLine(40f, y, 550f, y, paintBold)
            y += 20f

            for (item in listaOrdenada) {
                canvas.drawText(item.fecha,                       xFecha,   y, paint)
                canvas.drawText(item.nombreCliente,               xCliente, y, paint)
                canvas.drawText(formato.format(item.monto),       xMonto,   y, paint)
                canvas.drawText(formato.format(item.saldo),       xSaldo,   y, paint)

                y += 20f

                if (y > 800) {
                    document.finishPage(page)
                    y = 50f
                }
            }

            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()
        }

        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()

        // Compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }
}