package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.CotizadorUiState
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import com.example.appmuseo.utils.CotizadorEnvio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Pantalla del cotizador de envíos: toma los datos del formulario y usa CotizadorEnvio. */
class CotizadorViewModel : ViewModel() {

    private val _estado = MutableStateFlow(CotizadorUiState())
    val estado: StateFlow<CotizadorUiState> = _estado

    fun onTipoDestinoChange(tipo: TipoDestino) {
        // Al cambiar el tipo, la zona elegida ya no aplica
        _estado.update { it.copy(tipoDestino = tipo, zona = null, resultado = null, error = null) }
    }

    fun onZonaChange(zona: Zona) {
        _estado.update { it.copy(zona = zona, resultado = null, error = null) }
    }

    fun onPesoChange(valor: String) {
        _estado.update { it.copy(peso = valor, resultado = null, error = null) }
    }

    fun onMontoCompraChange(valor: String) {
        // Solo dejamos dígitos (el usuario podría escribir "$10.000")
        _estado.update { it.copy(montoCompra = valor.filter { c -> c.isDigit() }, resultado = null, error = null) }
    }

    fun calcular() {
        val actual = _estado.value
        val zona = actual.zona
        // toDoubleOrNull devuelve null si el texto no es un número (null safety, Guía 2)
        val peso = actual.peso.replace(',', '.').toDoubleOrNull()
        val monto = actual.montoCompra.toIntOrNull() ?: 0

        when {
            zona == null -> _estado.update { it.copy(error = "Selecciona una zona de destino", resultado = null) }
            peso == null -> _estado.update { it.copy(error = "Ingresa un peso válido (ej: 1,5)", resultado = null) }
            else -> {
                // try/catch: el cotizador lanza una excepción si el peso está fuera de rango (Guía 4)
                try {
                    val cotizacion = CotizadorEnvio.cotizar(zona, peso, monto)
                    _estado.update { it.copy(resultado = cotizacion, error = null) }
                } catch (e: IllegalArgumentException) {
                    _estado.update { it.copy(error = e.message, resultado = null) }
                }
            }
        }
    }
}
