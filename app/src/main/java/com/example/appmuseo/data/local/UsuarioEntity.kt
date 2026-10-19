package com.example.appmuseo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ROOM - Tabla "usuarios": cuentas creadas con el formulario de registro.
 * Seguridad desde el diseño: NUNCA se guarda la contraseña real, solo su hash SHA-256.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val correo: String,          // el correo identifica a la cuenta (no se repite)
    val nombre: String,
    val claveHash: String,
    val tipoUsuario: String,                 // Profesional, Estudiante o Público general
    val fechaRegistro: Long = System.currentTimeMillis()
)
