package com.example.appmuseo.data.remote.dto

// Lo que viaja en JSON entre la app y ms-usuarios.
// Los nombres de los campos son iguales a los del backend para que Gson los convierta solo

data class LoginRequestDto(
    val correo: String,
    val clave: String,
    val rol: String? = null
)

data class RegistroRequestDto(
    val nombre: String,
    val correo: String,
    val clave: String,
    val tipoUsuario: String,
    val avatar: Int
)

data class ActualizarPerfilDto(
    val nombre: String,
    val correo: String,
    val claveActual: String,
    val nuevaClave: String? = null,
    val avatar: Int? = null
)

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val tipoUsuario: String,
    val rol: String,
    val avatar: Int
)

// formato de error que devuelven los tres microservicios
data class ErrorDto(
    val estado: Int = 0,
    val mensaje: String? = null,
    val campos: Map<String, String>? = null
)
