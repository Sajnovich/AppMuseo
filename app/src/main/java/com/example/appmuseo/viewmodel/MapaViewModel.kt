package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.MapaUiState
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.MuseoCercano
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.utils.Distancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Mapa de Museos Virtuales: filtro por país, museo seleccionado y museo más cercano. */
class MapaViewModel(
    repositorio: MuseoRepository = MuseoRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow(MapaUiState(museos = repositorio.obtenerMuseos()))
    val estado: StateFlow<MapaUiState> = _estado

    /** null = mostrar todos los países. */
    fun seleccionarPais(pais: String?) {
        _estado.update { actual ->
            // Si el museo seleccionado no es del país elegido, se deselecciona
            val museo = actual.museoSeleccionado?.takeIf { pais == null || it.pais == pais }
            actual.copy(paisSeleccionado = pais, museoSeleccionado = museo)
        }
    }

    fun seleccionarMuseo(museo: Museo?) {
        _estado.update { it.copy(museoSeleccionado = museo) }
    }

    /**
     * Busca el museo más cercano a la ubicación recibida y lo deja seleccionado en el mapa.
     * Si latitud o longitud vienen null (sin permiso o sin señal) se usa la ubicación de
     * referencia, y el estado lo indica para avisarle al usuario.
     */
    fun buscarMuseoCercano(latitud: Double?, longitud: Double?) {
        val aproximada = latitud == null || longitud == null
        val lat = latitud ?: LATITUD_REFERENCIA
        val lon = longitud ?: LONGITUD_REFERENCIA

        val museo = Distancia.museoMasCercano(_estado.value.museos, lat, lon) ?: return
        val km = Distancia.kilometrosEntre(lat, lon, museo.latitud, museo.longitud)

        _estado.update {
            it.copy(
                paisSeleccionado = null,   // se quita el filtro para que el museo se vea en el mapa
                museoSeleccionado = museo,
                cercano = MuseoCercano(museo, km, aproximada)
            )
        }
    }

    companion object {
        // Ubicación de referencia (Santiago centro) cuando no hay ubicación real
        const val LATITUD_REFERENCIA = -33.45
        const val LONGITUD_REFERENCIA = -70.66
    }
}
