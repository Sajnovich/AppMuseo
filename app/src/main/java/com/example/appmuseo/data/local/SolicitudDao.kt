package com.example.appmuseo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ROOM - DAO (Data Access Object): aquí escribimos QUÉ consultas necesitamos.
 * Room genera el código SQL real (gracias a KSP) al compilar.
 */
@Dao
interface SolicitudDao {

    // Devuelve un Flow: cada vez que la tabla cambia, la lista se emite de nuevo
    // y la pantalla se actualiza sola (programación reactiva).
    @Query("SELECT * FROM solicitudes ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    // "suspend": las operaciones de BD no deben bloquear la interfaz
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Query("UPDATE solicitudes SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)
}
