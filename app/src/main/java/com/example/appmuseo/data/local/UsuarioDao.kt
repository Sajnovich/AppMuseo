package com.example.appmuseo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    // Devuelve null si no existe ninguna cuenta con ese correo
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correoOriginal = :correo LIMIT 1")
    suspend fun buscarPorCorreoOriginal(correo: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity)

    // el correo es la llave, por eso se actualiza con una query
    @Query("UPDATE usuarios SET correo = :nuevoCorreo, nombre = :nombre WHERE correo = :correoActual")
    suspend fun actualizarDatos(correoActual: String, nuevoCorreo: String, nombre: String)

    @Query("UPDATE usuarios SET claveHash = :claveHash WHERE correo = :correo")
    suspend fun actualizarClave(correo: String, claveHash: String)
}
