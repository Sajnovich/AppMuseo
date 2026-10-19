package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.model.LoginErrores
import com.example.appmuseo.model.LoginUiState
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Estado interno mutable / estado expuesto de solo
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errores = it.errores.copy(correo = null), resultado = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errores = it.errores.copy(clave = null), resultado = null) }
    }

    fun validarFormulario(): Boolean {
        val errores: LoginErrores = Validaciones.validarLogin(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    fun iniciarSesion() {
        if (!validarFormulario()) return
        if (_estado.value.resultado is ResultadoLogin.Autenticando) return

        viewModelScope.launch {
            _estado.update { it.copy(resultado = ResultadoLogin.Autenticando) }
            val resultado = authRepository.login(_estado.value.correo, _estado.value.clave)
            _estado.update { it.copy(resultado = resultado) }
        }
    }

    fun prepararTrasRegistro(correo: String) {
        _estado.value = LoginUiState(correo = correo, aviso = "¡Cuenta creada! Ingresa tu contraseña para entrar.")
    }

    fun limpiarFormulario() {
        _estado.value = LoginUiState()
    }
}
