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
    val anioLevantamiento: Int,   // ANO DEL LEVANTAMIENTO DE INFORMACION
    val calificacion: Double = 4.5,
    val resenas: Int = 0,
    val detalle: String = ""
) {
    // lo que va dentro del QR de cada museo, ej: MUSEO-1
    val codigoQr: String get() = "MUSEO-$id"

    fun etiquetas(): List<String> = listOf(tipo, pais, "Virtual")
}
