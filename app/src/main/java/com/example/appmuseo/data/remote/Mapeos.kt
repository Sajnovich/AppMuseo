package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.model.Rol

// Convierte lo que llega del backend (DTO) en los modelos que ya usan las pantallas.
// Así las pantallas no cambian aunque los datos ahora vengan del servidor

fun MuseoDto.aModelo(): Museo = Museo(
    id = id.toInt(),
    nombre = nombre,
    pais = pais,
    ciudad = ciudad,
    tipo = tipo,
    descripcion = descripcion,
    latitud = latitud,
    longitud = longitud,
    anioLevantamiento = anioLevantamiento,
    calificacion = calificacion,
    resenas = resenas,
    detalle = detalle ?: ""
)

fun ProductoDto.aModelo(): Producto {
    val categoriaModelo = Categoria.entries.find { it.name == categoria } ?: Categoria.MERCH
    return if (categoriaModelo == Categoria.CURSO) {
        Curso(
            id = id.toInt(),
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            cupos = stock,
            modalidad = modalidad ?: "Online",
            semanas = semanas ?: 1,
            destacado = destacado
        )
    } else {
        ProductoFisico(
            id = id.toInt(),
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            categoria = categoriaModelo,
            stock = stock,
            pesoKg = pesoKg,
            codigoBarra = codigoBarra ?: "",
            destacado = destacado
        )
    }
}

fun UsuarioDto.aPerfil(): PerfilUsuario = PerfilUsuario(
    nombre = nombre,
    correo = correo,
    tipoUsuario = tipoUsuario,
    rol = Rol.entries.find { it.name == rol } ?: Rol.CLIENTE,
    avatar = avatar
)
