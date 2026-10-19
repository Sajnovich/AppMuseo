package com.example.appmuseo.model

data class Museo(
    val id: Int,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val tipo: String,   // ARTE, HISTORIA, ARQUEOLOGÍA, CIENCIA
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val anioLevantamiento: Int   // ANO DEL LEVANTAMIENTO DE INFORMACION
)
