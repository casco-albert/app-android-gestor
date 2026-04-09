package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat

object ExportPDF {

    fun crearPDFEnDescargas(context: Context, lista: List<HistorialCobro>) {

        val listaOrdenada = lista.sortedByDescending { it.fecha }

        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpeta = File(baseDir, "HistorialCobPDF")
        if (!carpeta.exists()) carpeta.mkdirs()
        val formato = DecimalFormat("#,##0")

        val file = File(carpeta, "Historial_Cobros.pdf")

        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas

        val paint = Paint()
        paint.textSize = 12f

        val paintBold = Paint()
        paintBold.textSize = 13f
        paintBold.isFakeBoldText = true

        // 🔹 Posiciones de columnas (X)
        val xFecha = 40f
        val xCliente = 120f
        val xMonto = 340f
        val xPago = 340f
        val xSaldo = 450f

        var y = 50f

        canvas.drawText("Fecha", xFecha, y, paintBold)
        canvas.drawText("Cliente", xCliente, y, paintBold)
        canvas.drawText("Cantidad", xMonto, y, paintBold)
        canvas.drawText("Cantidad", xMonto, y, paintBold)
        canvas.drawText("Saldo", xSaldo, y, paintBold)

        y += 15f

        canvas.drawLine(40f, y, 550f, y, paintBold)

        y += 20f

        for (item in listaOrdenada) {

            canvas.drawText(item.fecha, xFecha, y, paint)
            canvas.drawText(item.nombreCliente, xCliente, y, paint)

            canvas.drawText(formato.format(item.monto), xMonto, y, paint)
            canvas.drawText(formato.format(item.saldo), xSaldo, y, paint)


            y += 20f

            // 🔹 Salto de página si se llena
            if (y > 800) {
                document.finishPage(page)
                y = 50f
            }
        }

        document.finishPage(page)
        document.writeTo(file.outputStream())
        document.close()

        Toast.makeText(context, "PDF guardado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

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