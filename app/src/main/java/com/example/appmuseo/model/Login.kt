package com.example.appmuseo.model

// roles del caso: cada uno ve solo sus funciones
enum class Rol(val etiqueta: String, val descripcion: String) {
    ADMINISTRADOR("Administrador", "Gestión completa del sistema"),
    INVESTIGADOR("Investigador", "Acceso a contenidos y proyectos"),
    CLIENTE("Cliente", "Explora, aprende y compra")
}

data class PerfilUsuario(
    val nombre: String,
    val correo: String,
    val tipoUsuario: String,
    val rol: Rol = Rol.CLIENTE,
    val avatar: Int = 0
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
    val aviso: String? = null,
    val rolSeleccionado: Rol? = null
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
    val avatar: Int = 0,
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

    // nombres de los campos con problemas, para el aviso de arriba del boton
    fun camposConError(): List<String> = listOfNotNull(
        if (nombre != null) "Nombre" else null,
        if (correo != null) "Correo" else null,
        if (clave != null) "Contraseña" else null,
        if (confirmarClave != null) "Repetir contraseña" else null,
        if (tipoUsuario != null) "Perfil" else null,
        if (terminos != null) "Términos y condiciones" else null
    )
}

// ---- Editar perfil ----
data class EditarPerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val claveActual: String = "",
    val cambiarClave: Boolean = false,
    val claveNueva: String = "",
    val repetirClave: String = "",
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val errorGeneral: String? = null,
    val errores: EditarPerfilErrores = EditarPerfilErrores()
)

data class EditarPerfilErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val claveActual: String? = null,
    val claveNueva: String? = null,
    val repetirClave: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(nombre, correo, claveActual, claveNueva, repetirClave).isNotEmpty()
}

sealed class ResultadoActualizacion {
    data class Exito(val perfil: PerfilUsuario) : ResultadoActualizacion()
    data class Error(val mensaje: String, val campo: String? = null) : ResultadoActualizacion()
}
