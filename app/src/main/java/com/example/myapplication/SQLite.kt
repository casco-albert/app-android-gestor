package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.text.SimpleDateFormat
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale
import kotlin.compareTo

class SQLite(context: Context) :
    SQLiteOpenHelper(context, "distripar", null, 6) {

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
                cli_id INTEGER,
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
                cli_id INTEGER,
                cob_fecha TEXT,
                monto REAL,
                saldo REAL
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
        SELECT a.id, a.nro_pedido, a.cli_id, b.nom, a.cantidad, a.kilos, a.precio, a.entrega
        FROM pedidos a
        INNER JOIN clientes b ON a.cli_id = b.id
        WHERE a.id_carga = ?
        ORDER BY a.nro_pedido
        """.trimIndent(),
            arrayOf(idCarga.toString())
        )

        val formato = DecimalFormat("#,###.0")

        if (cursor.moveToFirst()) {
            do {
                val pedido = Pedido(
                    id = cursor.getInt(0),
                    nroPedido = cursor.getString(1),
                    cli_id = cursor.getInt(2),             // ✅ aquí
                    cliente = cursor.getString(3),
                    cantidad = cursor.getInt(4),
                    kilos = cursor.getDouble(5),
                    precio = cursor.getDouble(6),
                    entrega = cursor.getInt(7)
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
    fun obtenerPedidosPorCarga(idCarga: Int): MutableList<Pedido> {

        val lista = mutableListOf<Pedido>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT a.id, a.nro_pedido, a.cli_id, b.nom, a.cantidad, a.kilos, a.precio, a.entrega
        FROM pedidos a
        INNER JOIN clientes b ON a.cli_id = b.id
        WHERE a.id_carga = ?
        ORDER BY a.nro_pedido
        """,
            arrayOf(idCarga.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val pedido = Pedido(
                    id = cursor.getInt(0),
                    nroPedido = cursor.getString(1),
                    cli_id = cursor.getInt(2),
                    cliente = cursor.getString(3),
                    cantidad = cursor.getInt(4),
                    kilos = cursor.getDouble(5),
                    precio = cursor.getDouble(6),
                    entrega = cursor.getInt(7)
                )
                lista.add(pedido)
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
    fun obtenerUltimoNroPedidoPorCarga(idCarga: Int): Int {
        val db = readableDatabase
        var ultimoNumero = 0

        val cursor = db.rawQuery(
            "SELECT MAX(nro_pedido) as max_nro FROM pedidos WHERE id_carga = ?",
            arrayOf(idCarga.toString())
        )

        if (cursor.moveToFirst()) {
            ultimoNumero = cursor.getInt(cursor.getColumnIndexOrThrow("max_nro"))
        }

        cursor.close()
        db.close()

        return ultimoNumero
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
    fun obtenerSaldoCliente(idCliente: Int): Double {

        val db = readableDatabase
        var saldo = 0.0

        val cursor = db.rawQuery(
            "SELECT totalDeuda FROM deuda WHERE cli_id = ? ORDER BY id DESC LIMIT 1",
            arrayOf(idCliente.toString())
        )

        if (cursor.moveToFirst()) {
            saldo = cursor.getDouble(0)
        }

        cursor.close()

        return saldo
    }
    fun generarDeuda(idPedido: Int, idCliente: Int, monto: Double) {

        val db = writableDatabase

        // Obtener saldo anterior del cliente, 0 si no tiene deuda
        val saldoAnterior = obtenerSaldoCliente(idCliente)

        // Calcular total de la deuda sumando el monto actual
        val totalDeuda = saldoAnterior + monto

        // Insertar registro en deuda
        val valores = ContentValues().apply {
            put("cli_id", idCliente)
            put("id_pedido", idPedido)
            put("deu_fecha", System.currentTimeMillis().toString())
            put("monto", monto)
            put("saldoAnterior", saldoAnterior)
            put("totalDeuda", totalDeuda)
        }

        db.insert("deuda", null, valores)
    }
    fun existeDeuda(idCliente: Cliente): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM deuda WHERE id_Cliente = ?",
            arrayOf(idCliente.toString())
        )

        val existe = cursor.count > 0

        cursor.close()

        return existe
    }
    fun obtenerDeudaPorCliente(): List<DeudaCliente> {

        val lista = mutableListOf<DeudaCliente>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT c.nom,
               d.deu_fecha,
               d.monto,
               d.saldoAnterior,
               d.totalDeuda
        FROM deuda d
        JOIN pedidos p ON d.id_pedido = p.id
        JOIN clientes c ON p.cli_id = c.id
        INNER JOIN (
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
                val fecha = cursor.getString(1)
                val monto = cursor.getDouble(2)
                val saldoAnterior = cursor.getDouble(3)
                val totalDeuda = cursor.getDouble(4)

                lista.add(
                    DeudaCliente(
                        cliente = cliente,
                        deuFecha = fecha,
                        monto = monto,
                        saldoAnterior = saldoAnterior,
                        totalDeuda = totalDeuda
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }
    fun obtenerClientesConDeuda(): MutableList<Cliente> {

        val lista = mutableListOf<Cliente>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT DISTINCT c.id, c.nom
        FROM clientes c
        JOIN deuda d ON c.id = d.cli_id
        WHERE d.totalDeuda > 0
        """,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val cliente = Cliente(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    doc = "",
                    direccion = "",
                    telefono = "",
                    precioKilo = 0.0
                )
                lista.add(cliente)
            } while (cursor.moveToNext())
        }

        cursor.close()

        return lista
    }
    fun obtenerHistorialCobros(clienteId: Int): List<HistorialCobro> {
        val lista = mutableListOf<HistorialCobro>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("""
            SELECT co.id, co.id_deuda, co.cli_id, c.nom, co.cob_fecha, co.monto, co.saldo
            FROM cobro co
            INNER JOIN clientes c ON co.cli_id = c.id
            WHERE co.cli_id = ?
            ORDER BY co.id DESC
        """.trimIndent(), arrayOf(clienteId.toString()))

            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        val id = c.getInt(0)
                        val idDeuda = c.getInt(1)
                        val idCliente = c.getInt(2)
                        val nombreCliente = c.getString(3)
                        val fechaMillis = c.getLong(4)
                        val monto = c.getDouble(5)
                        val saldo = c.getDouble(6)

                        val fechaFormateada = java.text.SimpleDateFormat(
                            "dd/MM/yyyy", java.util.Locale("es", "PY")
                        ).format(java.util.Date(fechaMillis))

                        lista.add(HistorialCobro(id, idDeuda, idCliente, nombreCliente, fechaFormateada, monto, saldo))
                    } while (c.moveToNext())
                }
            }
        }
        return lista
    }
    fun obtenerClienteIdPorDeuda(idDeuda: Int): Int {
        val db = readableDatabase
        var clienteId = 0

        val cursor = db.rawQuery(
            "SELECT cli_id FROM deuda WHERE id = ?",
            arrayOf(idDeuda.toString())
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                clienteId = c.getInt(0)
            }
        }

        return clienteId
    }
    fun obtenerHistorialCobrosTodos(): List<HistorialCobro> {
        val lista = mutableListOf<HistorialCobro>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("""
            SELECT co.id, co.id_deuda, co.cli_id, c.nom, co.cob_fecha, co.monto, co.saldo
            FROM cobro co
            INNER JOIN clientes c ON co.cli_id = c.id
            ORDER BY co.cob_fecha DESC
        """.trimIndent(), null)

            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        val id = c.getInt(0)
                        val idDeuda = c.getInt(1)
                        val idCliente = c.getInt(2)
                        val nombreCliente = c.getString(3)
                        val fechaMillis = c.getLong(4)
                        val monto = c.getDouble(5)
                        val saldo = c.getDouble(6)

                        val fechaFormateada = java.text.SimpleDateFormat(
                            "dd/MM/yyyy", java.util.Locale("es", "PY")
                        ).format(java.util.Date(fechaMillis))

                        lista.add(HistorialCobro(id, idDeuda, idCliente, nombreCliente, fechaFormateada, monto, saldo))
                    } while (c.moveToNext())
                }
            }
        }
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