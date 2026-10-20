package com.example.appmuseo.repository

import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(private val dao: SolicitudDao) {

    fun obtenerPorUsuario(correo: String): Flow<List<SolicitudEntity>> = dao.obtenerPorUsuario(correo)

    suspend fun insertar(solicitud: SolicitudEntity): Long = dao.insertar(solicitud)

    suspend fun actualizarEstado(id: Int, nuevoEstado: String) = dao.actualizarEstado(id, nuevoEstado)

    suspend fun cambiarDueno(anterior: String, nuevo: String) = dao.cambiarDueno(anterior, nuevo)

    suspend fun eliminar(solicitud: SolicitudEntity) = dao.eliminar(solicitud)
}
