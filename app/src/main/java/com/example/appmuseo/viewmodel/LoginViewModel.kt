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

/**
 * Lógica del LOGIN (mismo esquema de la Guía 11: estado + funciones onXChange + validar).
 *
 * El repositorio se recibe por parámetro con un valor por defecto: la app usa el real
 * y las pruebas unitarias pueden entregarle uno sin demora.
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Estado interno mutable / estado expuesto de solo lectura
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    // Al escribir, se actualiza el campo y se borra su error y el mensaje del "servidor"
    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errores = it.errores.copy(correo = null), resultado = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errores = it.errores.copy(clave = null), resultado = null) }
    }

    /** Valida los campos, guarda los errores en el estado y devuelve true si todo está bien. */
    fun validarFormulario(): Boolean {
        val errores: LoginErrores = Validaciones.validarLogin(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    /** Valida y, si corresponde, "consulta al servidor" en una corrutina. */
    fun iniciarSesion() {
        if (!validarFormulario()) return
        // Evita enviar dos veces si el usuario presiona rápido el botón
        if (_estado.value.resultado is ResultadoLogin.Autenticando) return

        viewModelScope.launch {
            _estado.update { it.copy(resultado = ResultadoLogin.Autenticando) }
            val resultado = authRepository.login(_estado.value.correo, _estado.value.clave)
            _estado.update { it.copy(resultado = resultado) }
        }
    }

    /** Después de crear una cuenta: deja el correo escrito y muestra un aviso positivo. */
    fun prepararTrasRegistro(correo: String) {
        _estado.value = LoginUiState(correo = correo, aviso = "¡Cuenta creada! Ingresa tu contraseña para entrar.")
    }

    /** Se llama después de entrar, para que el formulario quede limpio al cerrar sesión. */
    fun limpiarFormulario() {
        _estado.value = LoginUiState()
    }
}
