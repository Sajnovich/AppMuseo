package com.example.appmuseo.model

enum class TipoNotificacion(val etiqueta: String) {
    SOLICITUD("Solicitud"),
    CURSO("Curso"),
    NOVEDAD("Novedad"),
    MUSEO("Museo")
}

data class Notificacion(
    val id: Int,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoNotificacion,
    val hace: String,          // texto corto tipo "Hace 2 h"
    val leida: Boolean = false
)
