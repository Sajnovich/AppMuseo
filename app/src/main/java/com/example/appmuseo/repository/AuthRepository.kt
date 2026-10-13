package com.example.appmuseo.repository

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.local.UsuarioEntity
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.utils.Seguridad
import kotlinx.coroutines.delay

/** Usuario de prueba: correo, clave y su perfil. */
data class UsuarioPrueba(
    val correo: String,
    val clave: String,
    val perfil: PerfilUsuario
)

/**
 * Repositorio de autenticación SIMULADO.
 *
 * ⚠️ Las credenciales están guardadas en una VARIABLE (lista en memoria) porque
 * todavía no hay backend. Esto NO es seguro y solo sirve para el MVP de laboratorio.
 * En la Unidad 3 esta clase llamará a la API REST (Retrofit) y el resto de la app
 * no tendrá que cambiar: esa es la ventaja de separar en capas (MVVM + Repository).
 *
 * Además de los usuarios de prueba, revisa las cuentas creadas con el formulario de
 * registro, que se guardan en Room (tabla "usuarios") con la contraseña en hash.
 *
 * @param demoraMs tiempo que simula la "demora de red". En las pruebas usamos 0.
 * @param usuarioDao acceso a Room. Es opcional (null) para poder probar sin base de datos.
 */
class AuthRepository(
    private val demoraMs: Long = 1_500L,
    private val usuarioDao: UsuarioDao? = null
) {

    companion object {
        // Variables con los usuarios de prueba (reemplazan a la base de datos del servidor)
        val USUARIOS_PRUEBA = listOf(
            UsuarioPrueba(
                correo = "admin@museo.cl",
                clave = "museo123",
                perfil = PerfilUsuario("Administrador Demo", "admin@museo.cl", "Profesional")
            ),
            UsuarioPrueba(
                correo = "estudiante@museo.cl",
                clave = "duoc2026",
                perfil = PerfilUsuario("Estudiante Demo", "estudiante@museo.cl", "Estudiante")
            )
        )
    }

    /**
     * "suspend" = función que puede pausarse sin bloquear la pantalla (Guía 5: corrutinas).
     * Devuelve Exito con el perfil o Error con un mensaje.
     */
    suspend fun login(correo: String, clave: String): ResultadoLogin {
        delay(demoraMs) // Simula el tiempo de respuesta del servidor

        // El correo se compara sin espacios y en minúsculas; la clave debe ser exacta
        val correoLimpio = correo.trim().lowercase()

        // 1) Usuarios de prueba (variable)
        val usuarioPrueba = USUARIOS_PRUEBA.find { it.correo == correoLimpio && it.clave == clave }
        if (usuarioPrueba != null) return ResultadoLogin.Exito(usuarioPrueba.perfil)

        // 2) Cuentas registradas en el teléfono (Room): se compara el HASH, nunca la clave real
        val registrado = usuarioDao?.buscarPorCorreo(correoLimpio)
        if (registrado != null && registrado.claveHash == Seguridad.hashSha256(clave)) {
            return ResultadoLogin.Exito(
                PerfilUsuario(registrado.nombre, registrado.correo, registrado.tipoUsuario)
            )
        }
        return ResultadoLogin.Error("Correo o contraseña incorrectos")
    }

    /** Crea una cuenta nueva en Room. Falla si el correo ya está en uso. */
    suspend fun registrar(nombre: String, correo: String, clave: String, tipoUsuario: String): ResultadoRegistro {
        delay(demoraMs)
        // "?: return": si no hay base de datos, terminamos con un error
        val dao = usuarioDao ?: return ResultadoRegistro.Error("El registro no está disponible")
        val correoLimpio = correo.trim().lowercase()

        val yaExiste = USUARIOS_PRUEBA.any { it.correo == correoLimpio } || dao.buscarPorCorreo(correoLimpio) != null
        if (yaExiste) return ResultadoRegistro.Error("Ya existe una cuenta con ese correo")

        dao.insertar(
            UsuarioEntity(
                correo = correoLimpio,
                nombre = nombre.trim(),
                claveHash = Seguridad.hashSha256(clave),
                tipoUsuario = tipoUsuario
            )
        )
        return ResultadoRegistro.Exito(correoLimpio)
    }
}
