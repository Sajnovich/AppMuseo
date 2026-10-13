package com.example.appmuseo.utils

import com.example.appmuseo.model.CotizadorErrores
import com.example.appmuseo.model.CotizadorUiState
import com.example.appmuseo.model.LoginErrores
import com.example.appmuseo.model.LoginUiState
import com.example.appmuseo.model.RegistroErrores
import com.example.appmuseo.model.RegistroUiState
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

    // Códigos de producto del museo: MDM- seguido de 3 dígitos (ej: MDM-001)
    private val REGEX_CODIGO = Regex("^MDM-\\d{3}$", RegexOption.IGNORE_CASE)

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

    /** Para crear cuentas se exige además mezclar letras y números. */
    fun validarClaveSegura(clave: String): String? = when {
        clave.isBlank() -> "La contraseña es obligatoria"
        clave.length < LARGO_MINIMO_CLAVE -> "Debe tener al menos $LARGO_MINIMO_CLAVE caracteres"
        !clave.any { it.isLetter() } || !clave.any { it.isDigit() } -> "Debe combinar letras y números"
        else -> null
    }

    fun validarConfirmacion(clave: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Repite la contraseña"
        confirmacion != clave -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 letras"
        else -> null
    }

    /** Convierte el texto del peso a número. Acepta coma o punto decimal ("1,5" o "1.5"). */
    fun convertirPeso(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    fun validarPeso(texto: String): String? {
        val peso = convertirPeso(texto)
        return when {
            texto.isBlank() -> "Ingresa el peso del paquete"
            peso == null -> "Ingresa un número válido (ej: 1,5)"
            peso <= 0.0 -> "El peso debe ser mayor a 0 kg"
            peso > CotizadorEnvio.PESO_MAXIMO_KG -> "Máximo ${CotizadorEnvio.PESO_MAXIMO_KG.toInt()} kg por envío"
            else -> null
        }
    }

    fun validarCodigoProducto(codigo: String): String? = when {
        codigo.isBlank() -> "Escribe un código"
        !REGEX_CODIGO.matches(codigo.trim()) -> "Formato esperado: MDM-001"
        else -> null
    }

    /** Valida el formulario del cotizador completo. */
    fun validarCotizacion(estado: CotizadorUiState): CotizadorErrores = CotizadorErrores(
        zona = if (estado.zona == null) "Selecciona una zona de destino" else null,
        peso = validarPeso(estado.peso)
    )

    /** Valida el formulario de login completo. */
    fun validarLogin(estado: LoginUiState): LoginErrores = LoginErrores(
        correo = validarCorreo(estado.correo),
        clave = validarClave(estado.clave)
    )

    /** Valida el formulario de registro completo. */
    fun validarRegistro(estado: RegistroUiState): RegistroErrores = RegistroErrores(
        nombre = validarNombre(estado.nombre),
        correo = validarCorreo(estado.correo),
        clave = validarClaveSegura(estado.clave),
        confirmarClave = validarConfirmacion(estado.clave, estado.confirmarClave),
        tipoUsuario = if (estado.tipoUsuario == null) "Selecciona tu perfil" else null,
        terminos = if (!estado.aceptaTerminos) "Debes aceptar los términos y condiciones" else null
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
