package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat

object ExportPDFPedidos {

    fun crearPDFEnDescargas(context: Context, lista: List<Pedido>) {

        val formato = DecimalFormat("#,##0")

        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpeta = File(baseDir, "PedidosPDF")
        if (!carpeta.exists()) carpeta.mkdirs()

        val file = File(carpeta, "Pedidos.pdf")

        val document = PdfDocument()
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        var canvas = page.canvas

        val paint = Paint()
        val paintBold = Paint()

        paint.textSize = 10f
        paintBold.textSize = 11f
        paintBold.isFakeBoldText = true

        var y = 50f

        // 🔹 Posiciones de columnas (CLAVE)
        val colNro = 40f
        val colCliente = 90f
        val colCant = 250f
        val colKg = 310f
        val colTotal = 400f

        // 🔹 Encabezado
        canvas.drawText("Nro", colNro, y, paintBold)
        canvas.drawText("Cliente", colCliente, y, paintBold)
        canvas.drawText("Cant", colCant, y, paintBold)
        canvas.drawText("Kg", colKg, y, paintBold)
        canvas.drawText("Total", colTotal, y, paintBold)

        y += 15f

        // Línea separadora
        canvas.drawLine(40f, y, 550f, y, paint)
        y += 20f

        // 🔹 Datos
        for (p in lista) {

            canvas.drawText(p.nroPedido, colNro, y, paint)

            // Limitar cliente para que no rompa la tabla
            val cliente = if (p.cliente.length > 15) {
                p.cliente.substring(0, 15)
            } else {
                p.cliente
            }
            canvas.drawText(cliente, colCliente, y, paint)

            canvas.drawText(p.cantidad.toString(), colCant, y, paint)
            canvas.drawText(formato.format(p.kilos), colKg, y, paint)
            canvas.drawText(formato.format(p.precio), colTotal, y, paint)

            y += 18f

            // 🔹 Salto de página
            if (y > 800) {
                document.finishPage(page)

                page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
                canvas = page.canvas
                y = 50f
            }
        }

        document.finishPage(page)
        document.writeTo(file.outputStream())
        document.close()

        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()

        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }
}