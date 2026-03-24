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

object ExportPDF {

    fun crearPDFEnDescargas(context: Context, lista: List<HistorialCobro>) {

        val listaOrdenada = lista.sortedByDescending { it.fecha }

        // Carpeta Descargas
        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpeta = File(baseDir, "HistorialCobPDF")
        if (!carpeta.exists()) carpeta.mkdirs()

        val file = File(carpeta, "Historial_Cobros.pdf")

        // Crear PDF simple tipo lista
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 14f

        var y = 50f
        canvas.drawText("Fecha        Cliente                 Monto       Saldo", 50f, y, paint)
        y += 25f

        for (item in listaOrdenada) {
            val fecha = item.fecha.padEnd(12)
            val cliente = item.nombreCliente.padEnd(20)
            val monto = item.monto.toString().padEnd(10)
            val saldo = item.saldo.toString().padEnd(10)

            canvas.drawText("$fecha $cliente $monto $saldo", 50f, y, paint)
            y += 20f
        }

        document.finishPage(page)
        document.writeTo(file.outputStream())
        document.close()

        Toast.makeText(context, "PDF guardado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

        // 🔹 Compartir
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