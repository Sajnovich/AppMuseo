package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.MapaUiState
import com.example.appmuseo.model.Museo
import com.example.appmuseo.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Mapa de Museos Virtuales: filtro por país y museo seleccionado. */
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
}
