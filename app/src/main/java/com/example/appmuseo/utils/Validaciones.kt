package com.example.appmuseo.utils

import com.example.appmuseo.model.LoginErrores
import com.example.appmuseo.model.LoginUiState
import com.example.appmuseo.model.SolicitudErrores
import com.example.appmuseo.model.SolicitudUiState

/**
 * Reglas de validación de los formularios.
 *
 * Están en un "object" de Kotlin puro (sin nada de Android) a propósito:
 *  - se reutilizan en el login y en la solicitud de compra,
 *  - y se pueden probar con pruebas unitarias normales (carpeta test/).
 *
 * Cada función devuelve el MENSAJE de error, o null si el dato es válido.
 */
object Validaciones {

    // Expresión regular simple para correos: texto@dominio.extension
    // (Usamos Regex de Kotlin y no android.util.Patterns para poder testearla sin emulador)
    private val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

    const val LARGO_MINIMO_CLAVE = 6

    fun esCorreoValido(correo: String): Boolean = REGEX_CORREO.matches(correo.trim())

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !esCorreoValido(correo) -> "Formato de correo inválido (ej: nombre@correo.cl)"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "La contraseña es obligatoria"
        clave.length < LARGO_MINIMO_CLAVE -> "Debe tener al menos $LARGO_MINIMO_CLAVE caracteres"
        else -> null
    }

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 letras"
        else -> null
    }

    /** Valida el formulario de login completo. */
    fun validarLogin(estado: LoginUiState): LoginErrores = LoginErrores(
        correo = validarCorreo(estado.correo),
        clave = validarClave(estado.clave)
    )

    /**
     * Valida el formulario de solicitud.
     * Si el pedido solo tiene cursos (requiereEnvio = false) NO se pide dirección ni zona.
     */
    fun validarSolicitud(estado: SolicitudUiState, requiereEnvio: Boolean): SolicitudErrores =
        SolicitudErrores(
            nombre = validarNombre(estado.nombre),
            correo = validarCorreo(estado.correo),
            direccion = if (requiereEnvio && estado.direccion.isBlank()) "La dirección es obligatoria" else null,
            zona = if (requiereEnvio && estado.zona == null) "Selecciona una zona de despacho" else null,
            terminos = if (!estado.aceptaTerminos) "Debes aceptar los términos y condiciones" else null
        )
}
