package com.example.appmuseo.model

data class PerfilUsuario(
    val nombre: String,
    val correo: String,
    val tipoUsuario: String
)

sealed class ResultadoLogin {
    data class Exito(val perfil: PerfilUsuario) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
    object Autenticando : ResultadoLogin()
}

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores(),
    val resultado: ResultadoLogin? = null,
    val aviso: String? = null
)

data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(correo, clave).isNotEmpty()
}

// modelos del registro de usuarios

enum class TipoUsuario(val etiqueta: String) {
    PROFESIONAL("Profesional"),
    ESTUDIANTE("Estudiante"),
    PUBLICO_GENERAL("Público general")
}

sealed class ResultadoRegistro {
    data class Exito(val correo: String) : ResultadoRegistro()
    data class Error(val mensaje: String) : ResultadoRegistro()
    object Registrando : ResultadoRegistro()
}

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val tipoUsuario: TipoUsuario? = null,
    val aceptaTerminos: Boolean = false,
    val errores: RegistroErrores = RegistroErrores(),
    val resultado: ResultadoRegistro? = null
)

data class RegistroErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmarClave: String? = null,
    val tipoUsuario: String? = null,
    val terminos: String? = null
) {
    fun hayErrores(): Boolean =
        listOfNotNull(nombre, correo, clave, confirmarClave, tipoUsuario, terminos).isNotEmpty()
}
