package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DecimalFormat
import java.util.Locale
import kotlin.compareTo

class SQLite(context: Context) :
    SQLiteOpenHelper(context, "distripar", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                doc TEXT,
                nom TEXT,
                direc TEXT,
                telef TEXT,
                preciokilo REAL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE carga (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT,
                fecha TEXT,
                cantidadTotal REAL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE pedidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nro_pedido TEXT NOT NULL,
                cli_id INTEGER NOT NULL,
                id_carga INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                kilos REAL NOT NULL,
                precio REAL NOT NULL,
                entrega INTEGER DEFAULT 0,
                UNIQUE(cli_id, id_carga),
                FOREIGN KEY (cli_id) REFERENCES clientes(id) ON DELETE CASCADE,
                FOREIGN KEY (id_carga) REFERENCES carga(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE deuda (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_pedido INTEGER,
                deu_fecha TEXT,
                monto REAL,
                saldoAnterior REAL,
                totalDeuda REAL
            )
        """.trimIndent()
        )

        db.execSQL("""
            CREATE TABLE cobro (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_deuda INTEGER,
                cob_fecha TEXT,
                monto REAL
            )
        """.trimIndent()
        )

    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
    fun existePedidoEnCarga(idCliente: Int, idCarga: Int): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT id 
        FROM pedidos 
        WHERE cli_id = ? AND id_carga = ?
        """,
            arrayOf(idCliente.toString(), idCarga.toString())
        )

        val existe = cursor.count > 0

        cursor.close()

        return existe
    }
    fun obtenerClientes(): List<Cliente> {

        val lista = mutableListOf<Cliente>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM clientes", null)

        if (cursor.moveToFirst()) {
            do {

                val cliente = Cliente(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    doc = cursor.getString(cursor.getColumnIndexOrThrow("doc")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nom")),
                    direccion = cursor.getString(cursor.getColumnIndexOrThrow("direc")),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow("telef")),
                    precioKilo = cursor.getDouble(cursor.getColumnIndexOrThrow("preciokilo"))
                )

                lista.add(cliente)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return lista
    }

    fun editarCliente(id: Int, nuevoNombre: String, nuevoPrecioKilo: Double) {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("nom", nuevoNombre)
            put("preciokilo", nuevoPrecioKilo)
        }

        db.update("clientes", values, "id=?", arrayOf(id.toString()))

        db.close()
    }

    fun eliminarCliente(id: Int) {

        val db = writableDatabase
        db.delete("clientes", "id=?", arrayOf(id.toString()))
        db.close()

    }

    fun obtenerPedidos(): MutableList<Pedido> {

        val lista = mutableListOf<Pedido>()
        val db = readableDatabase

        val idCarga = obtenerUltimaCargaId()

        val cursor = db.rawQuery(
            """
        SELECT a.id, a.nro_pedido, b.nom, a.cantidad, a.kilos, a.precio, a.entrega
        FROM pedidos a
        INNER JOIN clientes b ON a.cli_id = b.id
        WHERE a.id_carga = ?
        ORDER BY a.nro_pedido
        """,
            arrayOf(idCarga.toString())
        )

        val formato = DecimalFormat("#,###.0")

        if (cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(0)
                val nroPedido = cursor.getString(1)
                val clienteNombre = cursor.getString(2)
                val cantidad = cursor.getInt(3)
                val kilosRaw = cursor.getDouble(4)
                val precioRaw = cursor.getDouble(5)
                val entrega = cursor.getInt(6)

                val pedido = Pedido(
                    id,
                    nroPedido,
                    clienteNombre,
                    cantidad,
                    kilosRaw,
                    precioRaw,
                    entrega
                )

                lista.add(pedido)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return lista
    }
    fun insertarCarga(descripcion: String, fecha: String, cantidadTotal: Double): Long {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("descripcion", descripcion)
            put("fecha", fecha)
            put("cantidadTotal", cantidadTotal)
        }

        val resultado = db.insert("carga", null, values)

        db.close()

        return resultado
    }
    fun obtenerCargas(): MutableList<Carga> {

        val lista = mutableListOf<Carga>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM carga ORDER BY fecha DESC", null)

        if (cursor.moveToFirst()) {
            do {

                val carga = Carga(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("cantidadTotal"))
                )

                lista.add(carga)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return lista
    }
    fun obtenerUltimaCargaId(): Int {

        val db = readableDatabase
        var idCarga = 0

        val cursor = db.rawQuery(
            "SELECT id FROM carga ORDER BY id DESC LIMIT 1",
            null
        )

        if (cursor.moveToFirst()) {
            idCarga = cursor.getInt(0)
        }

        cursor.close()

        return idCarga
    }
    fun marcarPedidoEntregado(idPedido: Int) {
        val db = writableDatabase

        val valores = ContentValues().apply {
            put("entrega", 1)
        }

        db.update(
            "pedidos",
            valores,
            "id=?",
            arrayOf(idPedido.toString())
        )

        db.close()
    }
    fun generarDeuda(idPedido: Int, monto: Double) {
        val db = writableDatabase

        // 1️⃣ Verificar si el pedido fue entregado
        val cursorPedido = db.rawQuery(
            "SELECT entrega FROM pedidos WHERE id = ?",
            arrayOf(idPedido.toString())
        )

        var entregado = false
        if (cursorPedido.moveToFirst()) {
            entregado = cursorPedido.getInt(0) == 1
        }
        cursorPedido.close()

        if (!entregado) {
            // Si no está entregado, no generar deuda
            db.close()
            return
        }

        // 2️⃣ Verificar saldo anterior
        var saldoAnterior = 0.0
        val cursorDeuda = db.rawQuery(
            "SELECT totalDeuda FROM deuda ORDER BY id DESC LIMIT 1",
            null
        )
        if (cursorDeuda.moveToFirst()) {
            saldoAnterior = cursorDeuda.getDouble(0)
        }
        cursorDeuda.close()

        val totalDeuda = saldoAnterior + monto

        // 3️⃣ Fecha actual
        val fechaActual = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        // 4️⃣ Insertar deuda
        val valores = ContentValues().apply {
            put("id_pedido", idPedido)
            put("deu_fecha", fechaActual)
            put("monto", monto)
            put("saldoAnterior", saldoAnterior)
            put("totalDeuda", totalDeuda)
        }

        db.insert("deuda", null, valores)
        db.close()
    }
    fun existeDeuda(idPedido: Int): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM deuda WHERE id_pedido = ?",
            arrayOf(idPedido.toString())
        )

        val existe = cursor.count > 0

        cursor.close()

        return existe
    }
    fun obtenerDeudaPorCliente(): MutableList<String> {

        val lista = mutableListOf<String>()
        val db = readableDatabase
        val formato = DecimalFormat.getNumberInstance(Locale("es", "PY"))

        lista.add("CLIENTE | ÚLTIMO MONTO | SALDO ANT | TOTAL DEUDA")

        // Consulta para traer la última deuda de cada cliente
        val cursor = db.rawQuery(
            """
        SELECT c.nom,
               d.monto,
               d.saldoAnterior,
               d.totalDeuda
        FROM deuda d
        JOIN pedidos p ON d.id_pedido = p.id
        JOIN clientes c ON p.cli_id = c.id
        INNER JOIN (
            -- traer la última deuda de cada cliente
            SELECT p.cli_id, MAX(d.id) AS ultima_deuda_id
            FROM deuda d
            JOIN pedidos p ON d.id_pedido = p.id
            GROUP BY p.cli_id
        ) ult ON ult.ultima_deuda_id = d.id
        ORDER BY c.nom
        """.trimIndent(),
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val cliente = cursor.getString(0)
                val montoUltimo = formato.format(cursor.getDouble(1))
                val saldoAnterior = formato.format(cursor.getDouble(2))
                val totalDeuda = formato.format(cursor.getDouble(3))

                lista.add("$cliente | $montoUltimo | $saldoAnterior | $totalDeuda")

            } while (cursor.moveToNext())
        }

        cursor.close()

        return lista
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS cobro")
        db.execSQL("DROP TABLE IF EXISTS deuda")
        db.execSQL("DROP TABLE IF EXISTS pedidos")
        db.execSQL("DROP TABLE IF EXISTS carga")
        db.execSQL("DROP TABLE IF EXISTS clientes")

        onCreate(db)

    }
}