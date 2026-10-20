package com.example.appmuseo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    // solo las del usuario que inicio sesion, la mas nueva primero
    @Query("SELECT * FROM solicitudes WHERE correoUsuario = :correo ORDER BY fecha DESC")
    fun obtenerPorUsuario(correo: String): Flow<List<SolicitudEntity>>

    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Query("UPDATE solicitudes SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    // si el usuario cambia su correo, su historial se va con el
    @Query("UPDATE solicitudes SET correoUsuario = :nuevo WHERE correoUsuario = :anterior")
    suspend fun cambiarDueno(anterior: String, nuevo: String)

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)
}
