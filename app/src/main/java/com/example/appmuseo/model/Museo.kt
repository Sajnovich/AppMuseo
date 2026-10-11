package com.example.appmuseo.model

/**
 * Museo virtual de Iberoamérica para el mapa.
 * Los datos son SINTÉTICOS (exigencia del caso: Leyes 21.719 y 19.628),
 * solo las coordenadas corresponden a ciudades reales para ubicarlos en el mapa.
 */
data class Museo(
    val id: Int,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val tipo: String,              // Arte, Historia, Arqueología, Ciencia...
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val anioLevantamiento: Int     // Año del levantamiento de información
)
