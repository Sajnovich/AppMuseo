package com.example.appmuseo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object EstadoSolicitud {
    const val PENDIENTE = "PENDIENTE"
    const val COMPLETADA = "COMPLETADA"
}

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // room asigna el id (1, 2, 3...)
    val tipo: String,
    val detalle: String,
    val nombreCliente: String,
    val correoCliente: String,
    val zonaEnvio: String,   // Vacío si solo hay cursos
    val direccion: String,
    val subtotal: Int,
    val costoEnvio: Int,
    val total: Int,
    val estado: String = EstadoSolicitud.PENDIENTE,
    val fecha: Long = System.currentTimeMillis()
)
