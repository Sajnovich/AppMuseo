package com.example.appmuseo.model

// ─────────────────────────────────────────────────────────────────────────────
// MODELOS DEL LOGIN (mismo enfoque de la Guía 11: UiState + Errores)
// ─────────────────────────────────────────────────────────────────────────────

/** Datos del usuario que obtenemos después de un login exitoso (Guía 5). */
data class PerfilUsuario(
    val nombre: String,
    val correo: String,
    val tipoUsuario: String   // Profesional, Estudiante o Público general
)

/**
 * Posibles resultados del login (Guía 5).
 * Una "sealed class" limita las opciones a estas tres: así el "when" sabe que no hay otras.
 */
sealed class ResultadoLogin {
    data class Exito(val perfil: PerfilUsuario) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
    object Autenticando : ResultadoLogin()
}

/** Estado completo de la pantalla de login. */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores(),
    val resultado: ResultadoLogin? = null,     // null = todavía no se ha intentado ingresar
    val aviso: String? = null                  // mensaje positivo, ej: "Cuenta creada"
)

/** Un mensaje de error por campo (null = el campo está bien). */
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(correo, clave).isNotEmpty()
}

// ─────────────────────────────────────────────────────────────────────────────
// MODELOS DEL REGISTRO DE USUARIOS
// ─────────────────────────────────────────────────────────────────────────────

/** Perfiles de usuario externos que define el caso. */
enum class TipoUsuario(val etiqueta: String) {
    PROFESIONAL("Profesional"),
    ESTUDIANTE("Estudiante"),
    PUBLICO_GENERAL("Público general")
}

/** Resultado de crear una cuenta (mismo patrón que ResultadoLogin). */
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
    val tipoUsuario: TipoUsuario? = null,       // null = todavía no elige
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
