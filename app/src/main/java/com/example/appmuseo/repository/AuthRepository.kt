package com.example.appmuseo.repository

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.local.UsuarioEntity
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.Rol
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

    suspend fun login(correo: String, clave: String): ResultadoLogin {
        delay(demoraMs)

        val correoLimpio = correo.trim().lowercase()

        val usuarioPrueba = USUARIOS_PRUEBA.find { it.correo == correoLimpio && it.clave == clave }
        if (usuarioPrueba != null) return ResultadoLogin.Exito(usuarioPrueba.perfil)

        val registrado = usuarioDao?.buscarPorCorreo(correoLimpio)
        if (registrado != null && registrado.claveHash == Seguridad.hashSha256(clave)) {
            return ResultadoLogin.Exito(
                PerfilUsuario(registrado.nombre, registrado.correo, registrado.tipoUsuario, Rol.CLIENTE, registrado.avatar)
            )
        }
        return ResultadoLogin.Error("Correo o contraseña incorrectos")
    }

    suspend fun registrar(nombre: String, correo: String, clave: String, tipoUsuario: String, avatar: Int = 0): ResultadoRegistro {
        delay(demoraMs)
        val dao = usuarioDao ?: return ResultadoRegistro.Error("El registro no está disponible")
        val correoLimpio = correo.trim().lowercase()

        val yaExiste = USUARIOS_PRUEBA.any { it.correo == correoLimpio } || dao.buscarPorCorreo(correoLimpio) != null
        if (yaExiste) return ResultadoRegistro.Error("Ya existe una cuenta con ese correo")

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
}
