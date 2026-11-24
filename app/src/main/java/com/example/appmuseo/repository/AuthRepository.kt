package com.example.appmuseo.repository

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.local.UsuarioEntity
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.TokenSesion
import com.example.appmuseo.data.remote.UsuariosApi
import com.example.appmuseo.data.remote.aPerfil
import com.example.appmuseo.data.remote.dto.ActualizarPerfilDto
import com.example.appmuseo.data.remote.dto.LoginRequestDto
import com.example.appmuseo.data.remote.dto.RegistroRequestDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.Rol
import com.example.appmuseo.model.ResultadoActualizacion
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.utils.Seguridad
import kotlinx.coroutines.delay

data class UsuarioPrueba(
    val correo: String,
    val clave: String,
    val perfil: PerfilUsuario
)

// Las cuentas viven en ms-usuarios (backend). Room guarda una copia para poder entrar sin conexión
class AuthRepository(
    private val demoraMs: Long = 1_500L,
    private val usuarioDao: UsuarioDao? = null,
    private val api: UsuariosApi? = null
) {

    companion object {
        val USUARIOS_PRUEBA = listOf(
            UsuarioPrueba(
                correo = "admin@museo.cl",
                clave = "museo123",
                perfil = PerfilUsuario("Administrador Demo", "admin@museo.cl", "Profesional", Rol.ADMINISTRADOR, 3)
            ),
            UsuarioPrueba(
                correo = "investigador@museo.cl",
                clave = "inves2026",
                perfil = PerfilUsuario("Investigadora Demo", "investigador@museo.cl", "Profesional", Rol.INVESTIGADOR, 1)
            ),
            UsuarioPrueba(
                correo = "estudiante@museo.cl",
                clave = "duoc2026",
                perfil = PerfilUsuario("Estudiante Demo", "estudiante@museo.cl", "Estudiante", Rol.CLIENTE, 2)
            )
        )
    }

    // La primera vez copia las cuentas de prueba a Room, asi tambien se pueden editar.
    // Se busca por correoOriginal para no volver a crearlas si la persona cambio su correo
    private suspend fun asegurarUsuariosPrueba(dao: UsuarioDao) {
        USUARIOS_PRUEBA.forEach { prueba ->
            if (dao.buscarPorCorreoOriginal(prueba.correo) == null && dao.buscarPorCorreo(prueba.correo) == null) {
                dao.insertar(
                    UsuarioEntity(
                        correo = prueba.correo,
                        nombre = prueba.perfil.nombre,
                        claveHash = Seguridad.hashSha256(prueba.clave),
                        tipoUsuario = prueba.perfil.tipoUsuario,
                        avatar = prueba.perfil.avatar,
                        rol = prueba.perfil.rol.name,
                        correoOriginal = prueba.correo
                    )
                )
            }
        }
    }

    private fun aPerfil(usuario: UsuarioEntity) = PerfilUsuario(
        nombre = usuario.nombre,
        correo = usuario.correo,
        tipoUsuario = usuario.tipoUsuario,
        rol = Rol.entries.find { it.name == usuario.rol } ?: Rol.CLIENTE,
        avatar = usuario.avatar
    )

    suspend fun login(correo: String, clave: String, rol: Rol? = null): ResultadoLogin {
        val correoLimpio = correo.trim().lowercase()

        // 1) primero se pregunta al backend: POST /api/usuarios/login
        val servicio = api
        if (servicio != null) {
            when (val respuesta = Red.llamar { servicio.login(LoginRequestDto(correoLimpio, clave, rol?.name)) }) {
                is ResultadoApi.Exito -> {
                    guardarCopiaLocal(respuesta.datos, clave)
                    // el token JWT queda listo para las siguientes peticiones
                    TokenSesion.guardar(respuesta.datos.token, respuesta.datos.expiraEn)
                    return ResultadoLogin.Exito(respuesta.datos.aPerfil())
                }
                is ResultadoApi.ErrorServidor -> return ResultadoLogin.Error(respuesta.mensaje)
                // 2) sin conexión se sigue con la copia guardada en el teléfono
                is ResultadoApi.SinConexion -> Unit
            }
        }

        delay(demoraMs)
        // sin conexión no hay token: se entra con la copia local, pero el backend pedirá iniciar sesión otra vez
        TokenSesion.limpiar()

        // sin base de datos (pruebas unitarias) se usa la lista fija
        val dao = usuarioDao
        if (dao == null) {
            val usuarioPrueba = USUARIOS_PRUEBA.find { it.correo == correoLimpio && it.clave == clave }
            return if (usuarioPrueba != null) ResultadoLogin.Exito(usuarioPrueba.perfil)
            else ResultadoLogin.Error("Correo o contraseña incorrectos")
        }

        asegurarUsuariosPrueba(dao)
        val registrado = dao.buscarPorCorreo(correoLimpio)
        if (registrado != null && registrado.claveHash == Seguridad.hashSha256(clave)) {
            return ResultadoLogin.Exito(aPerfil(registrado))
        }
        return ResultadoLogin.Error("Correo o contraseña incorrectos")
    }

    suspend fun registrar(nombre: String, correo: String, clave: String, tipoUsuario: String, avatar: Int = 0): ResultadoRegistro {
        // con backend la cuenta se crea en ms-usuarios: POST /api/usuarios/registro
        val servicio = api
        if (servicio != null) {
            val datos = RegistroRequestDto(nombre.trim(), correo.trim().lowercase(), clave, tipoUsuario, avatar)
            return when (val respuesta = Red.llamar { servicio.registrar(datos) }) {
                is ResultadoApi.Exito -> {
                    guardarCopiaLocal(respuesta.datos, clave)
                    ResultadoRegistro.Exito(respuesta.datos.correo)
                }
                // si falla una validación se muestra el mensaje del campo (ej: "Debe combinar letras y números")
                is ResultadoApi.ErrorServidor ->
                    ResultadoRegistro.Error(respuesta.campos.values.firstOrNull() ?: respuesta.mensaje)
                is ResultadoApi.SinConexion ->
                    ResultadoRegistro.Error("No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo")
            }
        }

        delay(demoraMs)
        val dao = usuarioDao ?: return ResultadoRegistro.Error("El registro no está disponible")
        val correoLimpio = correo.trim().lowercase()

        asegurarUsuariosPrueba(dao)
        if (dao.buscarPorCorreo(correoLimpio) != null) return ResultadoRegistro.Error("Ya existe una cuenta con ese correo")

        dao.insertar(
            UsuarioEntity(
                correo = correoLimpio,
                nombre = nombre.trim(),
                claveHash = Seguridad.hashSha256(clave),
                tipoUsuario = tipoUsuario,
                avatar = avatar
            )
        )
        return ResultadoRegistro.Exito(correoLimpio)
    }

    // Cambia nombre, correo y (si viene) la contraseña. Siempre pide la contraseña actual
    suspend fun actualizarPerfil(
        correoActual: String,
        nombre: String,
        nuevoCorreo: String,
        claveActual: String,
        nuevaClave: String? = null
    ): ResultadoActualizacion {
        val servicio = api
        if (servicio != null) {
            return actualizarEnServidor(servicio, correoActual, nombre, nuevoCorreo, claveActual, nuevaClave)
        }

        delay(demoraMs)
        val dao = usuarioDao ?: return ResultadoActualizacion.Error("La edición no está disponible")
        asegurarUsuariosPrueba(dao)

        val usuario = dao.buscarPorCorreo(correoActual.trim().lowercase())
            ?: return ResultadoActualizacion.Error("No encontramos tu cuenta")

        if (usuario.claveHash != Seguridad.hashSha256(claveActual)) {
            return ResultadoActualizacion.Error("La contraseña actual no es correcta", campo = "claveActual")
        }

        val correoLimpio = nuevoCorreo.trim().lowercase()
        if (correoLimpio != usuario.correo && dao.buscarPorCorreo(correoLimpio) != null) {
            return ResultadoActualizacion.Error("Ese correo ya está en uso", campo = "correo")
        }

        dao.actualizarDatos(usuario.correo, correoLimpio, nombre.trim())
        if (!nuevaClave.isNullOrBlank()) {
            dao.actualizarClave(correoLimpio, Seguridad.hashSha256(nuevaClave))
        }

        val actualizado = dao.buscarPorCorreo(correoLimpio) ?: return ResultadoActualizacion.Error("No se pudo guardar")
        return ResultadoActualizacion.Exito(aPerfil(actualizado))
    }

    // GET /api/usuarios/correo/{correo} para saber el id y luego PUT /api/usuarios/{id}
    private suspend fun actualizarEnServidor(
        servicio: UsuariosApi,
        correoActual: String,
        nombre: String,
        nuevoCorreo: String,
        claveActual: String,
        nuevaClave: String?
    ): ResultadoActualizacion {
        val cuenta = when (val busqueda = Red.llamar { servicio.buscarPorCorreo(correoActual.trim().lowercase()) }) {
            is ResultadoApi.Exito -> busqueda.datos
            is ResultadoApi.ErrorServidor -> return ResultadoActualizacion.Error(busqueda.mensaje)
            is ResultadoApi.SinConexion -> return ResultadoActualizacion.Error("Sin conexión: no se pudieron guardar los cambios")
        }
        val datos = ActualizarPerfilDto(
            nombre = nombre.trim(),
            correo = nuevoCorreo.trim().lowercase(),
            claveActual = claveActual,
            nuevaClave = nuevaClave?.takeIf { it.isNotBlank() },
            avatar = cuenta.avatar
        )
        return when (val respuesta = Red.llamar { servicio.actualizar(cuenta.id, datos) }) {
            is ResultadoApi.Exito -> {
                val dao = usuarioDao
                if (dao != null) {
                    try {
                        dao.actualizarDatos(correoActual.trim().lowercase(), respuesta.datos.correo, respuesta.datos.nombre)
                        guardarCopiaLocal(respuesta.datos, nuevaClave?.takeIf { it.isNotBlank() } ?: claveActual)
                    } catch (e: Exception) {
                        // la copia local es solo un respaldo: si falla no se pierde el cambio del servidor
                    }
                }
                ResultadoActualizacion.Exito(respuesta.datos.aPerfil())
            }
            is ResultadoApi.ErrorServidor -> when (respuesta.codigo) {
                401 -> ResultadoActualizacion.Error("La contraseña actual no es correcta", campo = "claveActual")
                409 -> ResultadoActualizacion.Error(respuesta.mensaje, campo = "correo")
                else -> ResultadoActualizacion.Error(respuesta.campos.values.firstOrNull() ?: respuesta.mensaje)
            }
            is ResultadoApi.SinConexion -> ResultadoActualizacion.Error("Sin conexión: no se pudieron guardar los cambios")
        }
    }

    // guarda o actualiza la cuenta en Room, así después se puede entrar sin conexión
    private suspend fun guardarCopiaLocal(usuario: UsuarioDto, clave: String) {
        val dao = usuarioDao ?: return
        try {
            val hash = Seguridad.hashSha256(clave)
            if (dao.buscarPorCorreo(usuario.correo) == null) {
                dao.insertar(
                    UsuarioEntity(
                        correo = usuario.correo,
                        nombre = usuario.nombre,
                        claveHash = hash,
                        tipoUsuario = usuario.tipoUsuario,
                        avatar = usuario.avatar,
                        rol = usuario.rol
                    )
                )
            } else {
                dao.actualizarDatos(usuario.correo, usuario.correo, usuario.nombre)
                dao.actualizarClave(usuario.correo, hash)
            }
        } catch (e: Exception) {
            // si Room falla igual se entra con los datos del servidor
        }
    }
}
