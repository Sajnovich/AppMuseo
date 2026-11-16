package com.example.appmuseo.data.remote

import com.example.appmuseo.data.remote.dto.ErrorDto
import com.google.gson.Gson
import retrofit2.Response
import kotlinx.coroutines.CancellationException
import java.io.IOException

// Resultado de una llamada al backend, sin excepciones sueltas por la app
sealed class ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>()

    // el servidor respondió con error (400, 401, 409...) y explica el motivo
    data class ErrorServidor(val codigo: Int, val mensaje: String, val campos: Map<String, String> = emptyMap()) :
        ResultadoApi<Nothing>()

    // no hubo respuesta: servidor apagado, sin internet o se demoró demasiado
    data class SinConexion(val mensaje: String = "No se pudo conectar con el servidor") : ResultadoApi<Nothing>()
}

object Red {

    private val gson = Gson()

    // envuelve una llamada de Retrofit y convierte cualquier resultado en ResultadoApi
    suspend fun <T> llamar(peticion: suspend () -> Response<T>): ResultadoApi<T> =
        try {
            val respuesta = peticion()
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                ResultadoApi.Exito(cuerpo)
            } else if (respuesta.isSuccessful) {
                // 204 No Content (ej: al eliminar): no trae cuerpo
                @Suppress("UNCHECKED_CAST")
                ResultadoApi.Exito(Unit as T)
            } else {
                val error = leerError(respuesta.errorBody()?.string())
                ResultadoApi.ErrorServidor(
                    codigo = respuesta.code(),
                    mensaje = error?.mensaje ?: "Error ${respuesta.code()} del servidor",
                    campos = error?.campos ?: emptyMap()
                )
            }
        } catch (e: CancellationException) {
            throw e   // si se cierra la pantalla se cancela la corrutina: no es un error
        } catch (e: IOException) {
            ResultadoApi.SinConexion()
        } catch (e: Exception) {
            ResultadoApi.ErrorServidor(-1, "Respuesta inesperada del servidor")
        }

    private fun leerError(json: String?): ErrorDto? =
        try {
            if (json.isNullOrBlank()) null else gson.fromJson(json, ErrorDto::class.java)
        } catch (e: Exception) {
            null
        }
}
