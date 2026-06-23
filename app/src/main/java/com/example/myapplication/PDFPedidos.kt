package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast

object PDFPedidos {

    fun crearPDFEnDescargas(context: Context, lista: List<Pedido>) {
        val document = buildDocument(lista)
        val fileName = "Pedidos_${System.currentTimeMillis()}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
                Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al crear el archivo", Toast.LENGTH_SHORT).show()
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(dir, fileName)
            file.outputStream().use { document.writeTo(it) }
            Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }

    fun generarBytes(context: Context, lista: List<Pedido>): ByteArray {
        val document = buildDocument(lista)
        val stream = java.io.ByteArrayOutputStream()
        document.writeTo(stream)
        document.close()
        return stream.toByteArray()
    }

    private fun buildDocument(lista: List<Pedido>): PdfDocument {
        val document = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        val paintBold = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val paintTitle = Paint().apply { textSize = 16f; isFakeBoldText = true }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = 40f
        val marginLeft = 40f

        canvas.drawText("Pedidos", marginLeft, y, paintTitle)
        y += 30f

        canvas.drawText("Cliente", marginLeft, y, paintBold)
        canvas.drawText("Cant.", 250f, y, paintBold)
        canvas.drawText("Kilos", 320f, y, paintBold)
        canvas.drawText("Total Gs.", 400f, y, paintBold)
        y += 20f

        canvas.drawLine(marginLeft, y, 555f, y, paint)
        y += 15f

        var totalCantidad = 0
        var totalKilos = 0.0
        var totalPrecio = 0.0

        lista.forEach { pedido ->
            canvas.drawText(pedido.nom, marginLeft, y, paint)
            canvas.drawText("${pedido.cantidad}", 250f, y, paint)
            canvas.drawText("%.0f".format(pedido.kilos), 320f, y, paint)
            canvas.drawText("%.0f".format(pedido.precio), 400f, y, paint)
            y += 18f

            totalCantidad += pedido.cantidad
            totalKilos += pedido.kilos
            totalPrecio += pedido.precio
        }

        y += 10f
        canvas.drawLine(marginLeft, y, 555f, y, paint)
        y += 15f
        canvas.drawText("TOTAL", marginLeft, y, paintBold)
        canvas.drawText("$totalCantidad", 250f, y, paintBold)
        canvas.drawText("%.0f".format(totalKilos), 320f, y, paintBold)
        canvas.drawText("%.0f".format(totalPrecio), 400f, y, paintBold)

        document.finishPage(page)
        return document
    }
}