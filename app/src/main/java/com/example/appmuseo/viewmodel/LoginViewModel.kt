package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.model.LoginErrores
import com.example.appmuseo.model.LoginUiState
import com.example.appmuseo.model.Rol
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

    fun onRolChange(rol: Rol?) {
        _estado.update { it.copy(rolSeleccionado = rol, resultado = null, errores = LoginErrores()) }
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
            var resultado = authRepository.login(_estado.value.correo, _estado.value.clave)
            // la cuenta tiene que ser del rol que eligio
            val rol = _estado.value.rolSeleccionado
            if (resultado is ResultadoLogin.Exito && rol != null && resultado.perfil.rol != rol) {
                resultado = ResultadoLogin.Error("Esta cuenta no es de ${rol.etiqueta}. Revisa el tipo de cuenta")
            }
            _estado.update { it.copy(resultado = resultado) }
        }
    }

    fun prepararTrasRegistro(correo: String) {
        _estado.value = LoginUiState(correo = correo, rolSeleccionado = Rol.CLIENTE, aviso = "¡Cuenta creada! Ingresa tu contraseña para entrar.")
    }

    fun limpiarFormulario() {
        _estado.value = LoginUiState()
    }
}
