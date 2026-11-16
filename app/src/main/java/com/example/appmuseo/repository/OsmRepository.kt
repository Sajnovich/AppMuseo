package com.example.appmuseo.repository

import com.example.appmuseo.data.remote.NominatimApi
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.LugarOsmDto

// API externa: OpenStreetMap (Nominatim) entrega la dirección real de unas coordenadas
class OsmRepository(private val api: NominatimApi) {

    // las coordenadas ya consultadas no se vuelven a pedir (Nominatim pide no repetir llamadas)
    private val consultadas = mutableMapOf<String, String>()

    suspend fun direccion(latitud: Double, longitud: Double): ResultadoApi<String> {
        val clave = "%.4f,%.4f".format(java.util.Locale.US, latitud, longitud)
        consultadas[clave]?.let { return ResultadoApi.Exito(it) }

        return when (val respuesta = Red.llamar { api.direccion(latitud, longitud) }) {
            is ResultadoApi.Exito -> {
                val texto = formatear(respuesta.datos)
                consultadas[clave] = texto
                ResultadoApi.Exito(texto)
            }
            is ResultadoApi.ErrorServidor -> respuesta
            is ResultadoApi.SinConexion -> respuesta
        }
    }

    companion object {
        // "Jirón de la Unión 300, Cercado de Lima, Lima, Perú"
        fun formatear(lugar: LugarOsmDto): String {
            val d = lugar.address
            val calle = listOfNotNull(d?.road, d?.numero).joinToString(" ").ifBlank { null }
            val partes = listOfNotNull(calle, d?.suburb, d?.city ?: d?.town, d?.state, d?.country).distinct()
            return if (partes.isNotEmpty()) partes.joinToString(", ") else lugar.nombreCompleto ?: "Dirección no disponible"
        }
    }
}
