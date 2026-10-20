package com.example.appmuseo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val correo: String,
    val nombre: String,
    val claveHash: String,
    val tipoUsuario: String,
    val avatar: Int = 0,
    val rol: String = "CLIENTE",
    val correoOriginal: String = "",   // para las cuentas de prueba: con que correo nacieron
    val fechaRegistro: Long = System.currentTimeMillis()
)
