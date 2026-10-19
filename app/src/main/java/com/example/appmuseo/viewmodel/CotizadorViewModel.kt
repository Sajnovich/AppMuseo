package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.CotizadorErrores
import com.example.appmuseo.model.CotizadorUiState
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Pantalla del cotizador de envíos: toma los datos del formulario y usa CotizadorEnvio. */
class CotizadorViewModel : ViewModel() {

    private val _estado = MutableStateFlow(CotizadorUiState())
    val estado: StateFlow<CotizadorUiState> = _estado

    fun onTipoDestinoChange(tipo: TipoDestino) {
        // Al cambiar el tipo, la zona elegida ya no aplica
        _estado.update { it.copy(tipoDestino = tipo, zona = null, resultado = null) }
    }

    // Cada cambio borra SOLO el error de su campo y el resultado anterior
    fun onZonaChange(zona: Zona) {
        _estado.update { it.copy(zona = zona, resultado = null, errores = it.errores.copy(zona = null)) }
    }

    fun onPesoChange(valor: String) {
        _estado.update { it.copy(peso = valor, resultado = null, errores = it.errores.copy(peso = null)) }
    }

    fun onMontoCompraChange(valor: String) {
        // Solo dejamos dígitos (el usuario podría escribir "$10.000")
        _estado.update { it.copy(montoCompra = valor.filter { c -> c.isDigit() }, resultado = null) }
    }

    fun calcular() {
        val actual = _estado.value
        // 1) Validación centralizada: un error por campo
        val errores = Validaciones.validarCotizacion(actual)
        if (errores.hayErrores()) {
            _estado.update { it.copy(errores = errores, resultado = null) }
            return
        }
        // 2) Datos ya validados ("?: return" solo por seguridad de tipos)
        val zona = actual.zona ?: return
        val peso = Validaciones.convertirPeso(actual.peso) ?: return
        val monto = actual.montoCompra.toIntOrNull() ?: 0

        // try/catch: el cotizador lanza una excepción si el peso está fuera de rango (Guía 4)
        try {
            val cotizacion = CotizadorEnvio.cotizar(zona, peso, monto)
            _estado.update { it.copy(resultado = cotizacion, errores = CotizadorErrores()) }
        } catch (e: IllegalArgumentException) {
            _estado.update { it.copy(errores = it.errores.copy(peso = e.message), resultado = null) }
        }
    }
}
