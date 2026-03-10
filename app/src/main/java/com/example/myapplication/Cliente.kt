package com.example.myapplication


data class Cliente(
    val id: Int,
    val doc: String,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val precioKilo: Double
)
data class ClienteItem(val id: Int, val nombre: String, val preciokilo: Double) {
    override fun toString(): String = nombre
}