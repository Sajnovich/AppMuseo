package com.example.appmuseo.repository

import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.ResultadoLogin
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
 * @param demoraMs tiempo que simula la "demora de red". En las pruebas usamos 0.
 */
class AuthRepository(private val demoraMs: Long = 1_500L) {

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
        val usuario = USUARIOS_PRUEBA.find { it.correo == correoLimpio && it.clave == clave }

        return if (usuario != null) {
            ResultadoLogin.Exito(usuario.perfil)
        } else {
            ResultadoLogin.Error("Correo o contraseña incorrectos")
        }
    }
}
