package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.model.RegistroUiState
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.model.TipoUsuario
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * REGISTRO DE USUARIOS (Guía 11): estado del formulario + validación centralizada en
 * Validaciones + guardado en Room a través del AuthRepository.
 */
class RegistroViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado

    // Cada cambio actualiza su campo y borra SOLO su propio error
    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null), resultado = null) }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errores = it.errores.copy(correo = null), resultado = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errores = it.errores.copy(clave = null), resultado = null) }
    }

    fun onConfirmarClaveChange(valor: String) {
        _estado.update { it.copy(confirmarClave = valor, errores = it.errores.copy(confirmarClave = null), resultado = null) }
    }

    fun onTipoUsuarioChange(tipo: TipoUsuario) {
        _estado.update { it.copy(tipoUsuario = tipo, errores = it.errores.copy(tipoUsuario = null)) }
    }

    fun onAceptaTerminosChange(valor: Boolean) {
        _estado.update { it.copy(aceptaTerminos = valor, errores = it.errores.copy(terminos = null)) }
    }

    fun validarFormulario(): Boolean {
        val errores = Validaciones.validarRegistro(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    fun registrar() {
        if (!validarFormulario()) return
        if (_estado.value.resultado is ResultadoRegistro.Registrando) return   // evita doble envío

        val actual = _estado.value
        viewModelScope.launch {
            _estado.update { it.copy(resultado = ResultadoRegistro.Registrando) }
            val resultado = authRepository.registrar(
                nombre = actual.nombre,
                correo = actual.correo,
                clave = actual.clave,
                tipoUsuario = actual.tipoUsuario?.etiqueta ?: ""
            )
            _estado.update { it.copy(resultado = resultado) }
        }
    }

    fun limpiarFormulario() {
        _estado.value = RegistroUiState()
    }
}
