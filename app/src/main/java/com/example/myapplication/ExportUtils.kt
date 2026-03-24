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
import java.io.OutputStreamWriter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormatSymbols
object ExportUtils {

    // =========================
    // 🔹 Funciones de formateo
    // =========================
    private fun formatearFecha(fecha: String): String {
        return try {
            if (fecha.all { it.isDigit() }) {
                val timestamp = fecha.toLong()
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formato.format(Date(timestamp))
            } else {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatoSalida.format(formatoEntrada.parse(fecha)!!)
            }
        } catch (e: Exception) {
            fecha
        }
    }

    private fun formatearNumero(numero: Double): String {
        val formato = DecimalFormat("#,##0")
        return formato.format(numero)
    }

    private fun formatearNumeroCSV(numero: Double): String {
        val formato = DecimalFormat("#,##0", DecimalFormatSymbols(java.util.Locale("es", "PY")))
        return formato.format(numero)
    }
    fun exportarCSVDeuda(context: Context, lista: List<DeudaCliente>) {

        val fileName = "deudas_${System.currentTimeMillis()}.csv"

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { output ->

                val writer = OutputStreamWriter(output)

                // Encabezado
                writer.append("Cliente,Fecha,Monto,Saldo Anterior,Total\n")

                // Datos formateados
                for (item in lista) {
                    writer.append("${item.cliente},")
                        .append("${formatearFecha(item.deuFecha)},")
                        .append("${formatearNumeroCSV(item.monto)};")
                        .append("${formatearNumeroCSV(item.saldoAnterior)};")
                        .append("${formatearNumeroCSV(item.totalDeuda)}\n")
                }

                writer.flush()
                writer.close()

                Toast.makeText(context, "CSV guardado en Descargas", Toast.LENGTH_LONG).show()
                compartirArchivo(context, it)
            }
        }
    }

    // =========================
    // ✅ EXPORTAR PDF
    // =========================
    fun exportarPDFDeuda(context: Context, lista: List<DeudaCliente>) {

        val fileName = "deudas_${System.currentTimeMillis()}.pdf"

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {

            val outputStream = resolver.openOutputStream(it)
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            var y = 40

            // Título
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Reporte de Deudas", 40f, y.toFloat(), paint)

            y += 30

            // Encabezado
            paint.textSize = 10f
            paint.isFakeBoldText = true

            canvas.drawText("Cliente", 40f, y.toFloat(), paint)
            canvas.drawText("Fecha", 150f, y.toFloat(), paint)
            canvas.drawText("Monto", 250f, y.toFloat(), paint)
            canvas.drawText("Saldo", 350f, y.toFloat(), paint)
            canvas.drawText("Total", 450f, y.toFloat(), paint)

            y += 20
            paint.isFakeBoldText = false

            // Datos formateados
            for (item in lista) {

                canvas.drawText(item.cliente, 40f, y.toFloat(), paint)
                canvas.drawText(formatearFecha(item.deuFecha), 150f, y.toFloat(), paint)
                canvas.drawText(formatearNumero(item.monto), 250f, y.toFloat(), paint)
                canvas.drawText(formatearNumero(item.saldoAnterior), 350f, y.toFloat(), paint)
                canvas.drawText(formatearNumero(item.totalDeuda), 450f, y.toFloat(), paint)

                y += 20

                // salto simple de página
                if (y > 800) break
            }

            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()
            outputStream?.close()

            Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            compartirArchivo(context, it)
        }
    }

    // =========================
    // ✅ COMPARTIR ARCHIVO
    // =========================
    private fun compartirArchivo(context: Context, uri: Uri) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(
            Intent.createChooser(intent, "Compartir archivo")
        )
    }
}