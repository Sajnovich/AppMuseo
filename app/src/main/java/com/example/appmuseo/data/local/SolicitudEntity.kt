package com.example.appmuseo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// el pedido avanza: pendiente -> confirmada -> enviada -> entregada
object EstadoSolicitud {
    const val PENDIENTE = "PENDIENTE"
    const val CONFIRMADA = "CONFIRMADA"
    const val ENVIADA = "ENVIADA"
    const val ENTREGADA = "ENTREGADA"

    val ORDEN = listOf(PENDIENTE, CONFIRMADA, ENVIADA, ENTREGADA)

    // null si ya esta entregada
    fun siguiente(estado: String): String? = ORDEN.getOrNull(ORDEN.indexOf(estado) + 1)

    fun etiqueta(estado: String): String = when (estado) {
        PENDIENTE -> "Pendiente"
        CONFIRMADA -> "Confirmada"
        ENVIADA -> "Enviada"
        ENTREGADA -> "Entregada"
        else -> estado
    }
}

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // room asigna el id (1, 2, 3...)
    val correoUsuario: String = "",   // dueño de la solicitud, asi cada perfil ve solo las suyas
    val tipo: String,
    val detalle: String,
    val lineas: String = "",   // productos con cantidad y precio (ver model/Boleta)
    val cantidadProductos: Int = 1,
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

// numero "bonito" para mostrar, ej: #2026-00007
fun codigoSolicitud(id: Int): String = "#2026-" + id.toString().padStart(5, '0')
