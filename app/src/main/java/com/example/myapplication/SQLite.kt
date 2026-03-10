package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DecimalFormat

class SQLite(context: Context) :
    SQLiteOpenHelper(context, "distripar", null, 3) {

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
                FOREIGN KEY (cli_id) REFERENCES clientes(id) ON DELETE CASCADE,
                FOREIGN KEY (id_carga) REFERENCES carga(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )
        db.execSQL("""
            CREATE TABLE deuda (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_pedido INTEGER,
                deu_fecha TEXT,
                monto REAL,
                saldoAnterior REAL,
                totalDeuda REAL
            )
        """)

        db.execSQL("""
            CREATE TABLE cobro (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_deuda INTEGER,
                cob_fecha TEXT,
                monto REAL
            )
        """)

    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
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

        val cursor = db.rawQuery(
            """
        SELECT a.id, a.nro_pedido, b.nom, a.cantidad, a.kilos, a.precio, a.entrega
        FROM pedidos a
        INNER JOIN clientes b ON a.cli_id = b.id
        ORDER BY a.nro_pedido
        """,
            null
        )

        val formato = DecimalFormat("#,###.0")

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nroPedido = cursor.getString(1)
                val clienteNombre = cursor.getString(2)
                val cantidad = cursor.getInt(3)

                // Formatear kilos y precio para mostrar
                val kilosRaw = cursor.getDouble(4)
                val precioRaw = cursor.getDouble(5)
                val entrega = cursor.getInt(6)

                val kilosFormateado = formato.format(kilosRaw)
                val precioFormateado = formato.format(precioRaw)

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
        db.close()

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
    fun generarDeuda(idPedido: Int, monto: Double) {

        val db = this.writableDatabase

        var saldoAnterior = 0.0

        val cursor = db.rawQuery(
            "SELECT totalDeuda FROM deuda ORDER BY id DESC LIMIT 1",
            null
        )

        if (cursor.moveToFirst()) {
            saldoAnterior = cursor.getDouble(0)
        }

        cursor.close()

        val totalDeuda = saldoAnterior + monto

        val valores = ContentValues()
        valores.put("id_pedido", idPedido)
        valores.put("deu_fecha", System.currentTimeMillis().toString())
        valores.put("monto", monto)
        valores.put("saldoAnterior", saldoAnterior)
        valores.put("totalDeuda", totalDeuda)

        db.insert("deuda", null, valores)
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
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 3) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS carga (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    descripcion TEXT,
                    fecha TEXT,
                    cantidadTotal REAL
                )
            """.trimIndent()
            )

            db.execSQL("ALTER TABLE pedidos ADD COLUMN id_carga INTEGER")
            db.execSQL("ALTER TABLE pedidos ADD COLUMN entrega BOOLEAN")
        }

    }
}