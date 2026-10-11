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
    val resultado: ResultadoLogin? = null      // null = todavía no se ha intentado ingresar
)

/** Un mensaje de error por campo (null = el campo está bien). */
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(correo, clave).isNotEmpty()
}
