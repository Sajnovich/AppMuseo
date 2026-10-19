package com.example.appmuseo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    // devuelve un flow
    @Query("SELECT * FROM solicitudes ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Query("UPDATE solicitudes SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)
}
