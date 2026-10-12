package com.example.appmuseo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Estados de avance de una solicitud (pendiente o completada, como pide el caso). */
object EstadoSolicitud {
    const val PENDIENTE = "PENDIENTE"
    const val COMPLETADA = "COMPLETADA"
}

/**
 * ROOM - Entidad: cada objeto de esta clase es UNA FILA de la tabla "solicitudes".
 * Room crea la tabla automáticamente a partir de las propiedades.
 */
@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Room asigna el id (1, 2, 3...)
    val tipo: String,              // COMPRA, INSCRIPCIÓN o COMPRA + INSCRIPCIÓN
    val detalle: String,           // Ej: "2x Taza..., 1x Curso..."
    val nombreCliente: String,
    val correoCliente: String,
    val zonaEnvio: String,         // Vacío si solo hay cursos
    val direccion: String,
    val subtotal: Int,
    val costoEnvio: Int,
    val total: Int,
    val estado: String = EstadoSolicitud.PENDIENTE,
    val fecha: Long = System.currentTimeMillis()   // Fecha de registro en milisegundos
)
