package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.model.EstadoRed
import kotlinx.coroutines.launch
import com.example.appmuseo.model.MapaUiState
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.MuseoCercano
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.utils.Distancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapaViewModel(
    private val repositorio: MuseoRepository = MuseoRepository(RetrofitInstance.catalogo)
) : ViewModel() {

    private val _estado = MutableStateFlow(MapaUiState(museos = repositorio.obtenerMuseos()))
    val estado: StateFlow<MapaUiState> = _estado

    // Pide los datos a ms-catalogo con Retrofit. Si el servidor no responde se sigue con la copia local
    fun sincronizar() {
        if (_estado.value.red.cargando) return
        _estado.update { it.copy(red = it.red.copy(cargando = true, aviso = null)) }
        viewModelScope.launch {
            val resultado = repositorio.sincronizar()
            _estado.update {
                it.copy(
                    museos = repositorio.obtenerMuseos(),
                    red = EstadoRed(
                        cargando = false,
                        desdeServidor = resultado is ResultadoApi.Exito,
                        aviso = when (resultado) {
                            is ResultadoApi.Exito -> null
                            is ResultadoApi.ErrorServidor -> resultado.mensaje
                            is ResultadoApi.SinConexion -> "Sin conexión con el servidor: mostrando datos guardados"
                        }
                    )
                )
            }
        }
    }

    fun seleccionarPais(pais: String?) {
        _estado.update { actual ->
            // Si el museo seleccionado no es del país elegido, se
            val museo = actual.museoSeleccionado?.takeIf { pais == null || it.pais == pais }
            actual.copy(paisSeleccionado = pais, museoSeleccionado = museo)
        }
    }

    fun seleccionarTipo(tipo: String?) {
        _estado.update { actual ->
            val museo = actual.museoSeleccionado?.takeIf { tipo == null || it.tipo == tipo }
            actual.copy(tipoSeleccionado = tipo, museoSeleccionado = museo)
        }
    }

    // boton "Todos"
    fun limpiarFiltros() {
        _estado.update { it.copy(paisSeleccionado = null, tipoSeleccionado = null) }
    }

    fun buscarMuseo(id: Int): Museo? = repositorio.buscarPorId(id)

    fun seleccionarMuseo(museo: Museo?) {
        _estado.update { it.copy(museoSeleccionado = museo) }
    }

    fun buscarMuseoCercano(latitud: Double?, longitud: Double?) {
        val aproximada = latitud == null || longitud == null
        val lat = latitud ?: LATITUD_REFERENCIA
        val lon = longitud ?: LONGITUD_REFERENCIA

        val museo = Distancia.museoMasCercano(_estado.value.museos, lat, lon) ?: return
        val km = Distancia.kilometrosEntre(lat, lon, museo.latitud, museo.longitud)

        _estado.update {
            it.copy(
                paisSeleccionado = null,
                tipoSeleccionado = null,
                museoSeleccionado = museo,
                cercano = MuseoCercano(museo, km, aproximada)
            )
        }
    }

    companion object {
        const val LATITUD_REFERENCIA = -33.45
        const val LONGITUD_REFERENCIA = -70.66
    }
}
