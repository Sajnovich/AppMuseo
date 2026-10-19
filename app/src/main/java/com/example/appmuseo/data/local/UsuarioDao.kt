package com.example.appmuseo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** ROOM - Consultas de la tabla "usuarios". */
@Dao
interface UsuarioDao {

    // Devuelve null si no existe ninguna cuenta con ese correo
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    // ABORT: si el correo ya existe, Room lanza un error en vez de sobrescribir la cuenta
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity)
}
