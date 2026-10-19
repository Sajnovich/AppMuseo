package com.example.appmuseo.repository

import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(private val dao: SolicitudDao) {

    fun obtenerTodas(): Flow<List<SolicitudEntity>> = dao.obtenerTodas()

    suspend fun insertar(solicitud: SolicitudEntity): Long = dao.insertar(solicitud)

    suspend fun actualizarEstado(id: Int, nuevoEstado: String) = dao.actualizarEstado(id, nuevoEstado)

    suspend fun eliminar(solicitud: SolicitudEntity) = dao.eliminar(solicitud)
}
