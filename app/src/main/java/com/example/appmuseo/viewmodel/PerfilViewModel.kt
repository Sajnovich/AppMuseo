package com.example.appmuseo.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * PERFIL: foto (cámara / galería, Guía 13) y preferencia de notificaciones (DataStore, Guía 12).
 */
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val sesionDataStore = SesionDataStore(application)

    // Uri de la imagen elegida o capturada (null = sin foto → se muestra un ícono)
    private val _imagenUri = MutableStateFlow<Uri?>(null)
    val imagenUri: StateFlow<Uri?> = _imagenUri

    // null mientras se lee DataStore (igual que en la Guía 12)
    private val _notificacionesActivas = MutableStateFlow<Boolean?>(null)
    val notificacionesActivas: StateFlow<Boolean?> = _notificacionesActivas

    private val _mostrarMensaje = MutableStateFlow(false)
    val mostrarMensaje: StateFlow<Boolean> = _mostrarMensaje

    init {
        viewModelScope.launch {
            _notificacionesActivas.value = sesionDataStore.obtenerNotificaciones().first()
        }
    }

    fun actualizarImagen(uri: Uri) {
        _imagenUri.value = uri
    }

    fun alternarNotificaciones() {
        viewModelScope.launch {
            val nuevoValor = !(_notificacionesActivas.value ?: true)
            sesionDataStore.guardarNotificaciones(nuevoValor)   // se guarda en el teléfono
            _notificacionesActivas.value = nuevoValor

            _mostrarMensaje.value = true                         // mensaje animado
            delay(2_000)
            _mostrarMensaje.value = false
        }
    }
}
