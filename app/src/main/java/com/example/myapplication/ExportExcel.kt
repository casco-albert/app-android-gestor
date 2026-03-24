package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
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
        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val carpeta = File(baseDir, "HistorialCobCSV")

        if (!carpeta.exists()) carpeta.mkdirs()

        val file = File(carpeta, "Historial_Cobros.csv")

        file.printWriter().use { out ->
            out.println("Cliente,Fecha,Monto,Saldo")
            for (item in lista) {
                out.println("${item.nombreCliente},${item.fecha},${formato.format(item.monto)},${formato.format(item.saldo)}")
            }
        }

        Toast.makeText(context, "CSV guardado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

        // 🔹 Compartir por WhatsApp o cualquier app
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider", // Debes declarar en Manifest
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir CSV"))
    }
}