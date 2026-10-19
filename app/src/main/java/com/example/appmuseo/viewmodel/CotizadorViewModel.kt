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

class CotizadorViewModel : ViewModel() {

    private val _estado = MutableStateFlow(CotizadorUiState())
    val estado: StateFlow<CotizadorUiState> = _estado

    fun onTipoDestinoChange(tipo: TipoDestino) {
        // AL CAMBIAR EL TIPO, LA ZONA ELEGIDA YA NO APLICA
        _estado.update { it.copy(tipoDestino = tipo, zona = null, resultado = null) }
    }

    fun onZonaChange(zona: Zona) {
        _estado.update { it.copy(zona = zona, resultado = null, errores = it.errores.copy(zona = null)) }
    }

    fun onPesoChange(valor: String) {
        _estado.update { it.copy(peso = valor, resultado = null, errores = it.errores.copy(peso = null)) }
    }

    fun onMontoCompraChange(valor: String) {
        _estado.update { it.copy(montoCompra = valor.filter { c -> c.isDigit() }, resultado = null) }
    }

    fun calcular() {
        val actual = _estado.value
        val errores = Validaciones.validarCotizacion(actual)
        if (errores.hayErrores()) {
            _estado.update { it.copy(errores = errores, resultado = null) }
            return
        }
        // 2) Datos ya validados ("?: return" solo por seguridad
        val zona = actual.zona ?: return
        val peso = Validaciones.convertirPeso(actual.peso) ?: return
        val monto = actual.montoCompra.toIntOrNull() ?: 0

        try {
            val cotizacion = CotizadorEnvio.cotizar(zona, peso, monto)
            _estado.update { it.copy(resultado = cotizacion, errores = CotizadorErrores()) }
        } catch (e: IllegalArgumentException) {
            _estado.update { it.copy(errores = it.errores.copy(peso = e.message), resultado = null) }
        }
    }
}
