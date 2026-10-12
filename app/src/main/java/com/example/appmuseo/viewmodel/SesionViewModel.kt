package com.example.appmuseo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.model.PerfilUsuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Maneja la SESIÓN del usuario con DataStore (Guía 12).
 * Es AndroidViewModel porque DataStore necesita el "context" de la aplicación.
 */
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val sesionDataStore = SesionDataStore(application)

    // null = todavía estamos leyendo DataStore (se muestra la pantalla de carga)
    private val _sesionActiva = MutableStateFlow<Boolean?>(null)
    val sesionActiva: StateFlow<Boolean?> = _sesionActiva

    private val _usuario = MutableStateFlow(PerfilUsuario("", "", ""))
    val usuario: StateFlow<PerfilUsuario> = _usuario

    init {
        cargarSesion()
    }

    private fun cargarSesion() {
        viewModelScope.launch {
            delay(1_200) // Simula la carga inicial para mostrar el CircularProgressIndicator
            // first() toma el valor ACTUAL del Flow y termina
            val activa = sesionDataStore.obtenerSesionActiva().first() ?: false
            if (activa) {
                _usuario.value = sesionDataStore.obtenerUsuario().first()
            }
            _sesionActiva.value = activa
        }
    }

    fun iniciarSesion(perfil: PerfilUsuario) {
        viewModelScope.launch {
            sesionDataStore.guardarSesion(perfil)
            _usuario.value = perfil
            _sesionActiva.value = true
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sesionDataStore.cerrarSesion()
            _usuario.value = PerfilUsuario("", "", "")
            _sesionActiva.value = false
        }
    }
}
