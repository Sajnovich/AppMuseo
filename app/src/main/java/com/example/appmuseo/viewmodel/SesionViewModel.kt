package com.example.appmuseo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.remote.TokenSesion
import com.example.appmuseo.model.PerfilUsuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val sesionDataStore = SesionDataStore(application)

    private val _sesionActiva = MutableStateFlow<Boolean?>(null)
    val sesionActiva: StateFlow<Boolean?> = _sesionActiva

    private val _usuario = MutableStateFlow(PerfilUsuario("", "", ""))
    val usuario: StateFlow<PerfilUsuario> = _usuario

    init {
        cargarSesion()
    }

    private fun cargarSesion() {
        viewModelScope.launch {
            delay(1_200)
            var activa = sesionDataStore.obtenerSesionActiva().first() ?: false
            if (activa) {
                // se recupera el token JWT guardado para seguir usando el backend
                val (token, expira) = sesionDataStore.obtenerToken().first()
                if (token.isNullOrBlank()) {
                    // sesión antigua o sin conexión: sin token el backend responde 401, así que se pide entrar de nuevo
                    activa = false
                } else {
                    TokenSesion.guardar(token, expira)
                    _usuario.value = sesionDataStore.obtenerUsuario().first()
                }
            }
            _sesionActiva.value = activa
        }
    }

    fun iniciarSesion(perfil: PerfilUsuario) {
        _usuario.value = perfil
        _sesionActiva.value = true
        viewModelScope.launch {
            sesionDataStore.guardarSesion(perfil)
            sesionDataStore.guardarToken(TokenSesion.token, TokenSesion.expiraEn)
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sesionDataStore.cerrarSesion()
            TokenSesion.limpiar()
            _usuario.value = PerfilUsuario("", "", "")
            _sesionActiva.value = false
        }
    }
}
