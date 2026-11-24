package com.example.appmuseo.data.remote.dto

data class MuseoDto(
    val id: Long,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val tipo: String,
    val descripcion: String,
    val detalle: String?,
    val latitud: Double,
    val longitud: Double,
    val anioLevantamiento: Int,
    val calificacion: Double,
    val resenas: Int
)

data class ProductoDto(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val categoria: String,   // MERCH, CONSERVACION, PUBLICACION, CURSO
    val stock: Int,
    val pesoKg: Double,
    val codigoBarra: String?,
    val modalidad: String?,
    val semanas: Int?,
    val destacado: Boolean
)

// lo que se envía al crear o editar (POST / PUT). El id no va: lo decide el servidor

data class ProductoRequestDto(
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val categoria: String,
    val stock: Int,
    val pesoKg: Double,
    val codigoBarra: String?,
    val modalidad: String?,
    val semanas: Int?,
    val destacado: Boolean
)

data class MuseoRequestDto(
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val tipo: String,
    val descripcion: String,
    val detalle: String,
    val latitud: Double,
    val longitud: Double,
    val anioLevantamiento: Int,
    val calificacion: Double,
    val resenas: Int
)
