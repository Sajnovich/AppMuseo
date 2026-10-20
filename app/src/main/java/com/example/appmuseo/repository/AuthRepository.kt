package com.example.appmuseo.repository

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.local.UsuarioEntity
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

class AuthRepository(
    private val demoraMs: Long = 1_500L,
    private val usuarioDao: UsuarioDao? = null
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

    suspend fun login(correo: String, clave: String): ResultadoLogin {
        delay(demoraMs)
        val correoLimpio = correo.trim().lowercase()

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
}
