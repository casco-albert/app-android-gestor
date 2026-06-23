package com.example.myapplication

import com.google.gson.annotations.SerializedName


data class Cliente(
    val id: Int = 0,
    val rec: Double,
    val nom: String,
    val direc: String,
    val telef: String,
    val preciokilo: Double,
)
data class ClienteItem(val id: Int, val nom: String, val preciokilo: Double) {
    override fun toString(): String = nom
}