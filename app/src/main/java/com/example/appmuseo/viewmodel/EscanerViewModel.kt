package com.example.appmuseo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EscanerViewModel(
    private val repositorio: CatalogoRepository = CatalogoRepository()
) : ViewModel() {

    private val _fotoUri = MutableStateFlow<Uri?>(null)
    val fotoUri: StateFlow<Uri?> = _fotoUri

    // Solo un ProductoFisico tiene código de barras, por eso
    private val _productoDetectado = MutableStateFlow<ProductoFisico?>(null)
    val productoDetectado: StateFlow<ProductoFisico?> = _productoDetectado

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _codigo = MutableStateFlow("")
    val codigo: StateFlow<String> = _codigo

    private val _errorCodigo = MutableStateFlow<String?>(null)
    val errorCodigo: StateFlow<String?> = _errorCodigo

    private var indiceSimulado = 0

    fun onFotoCapturada(uri: Uri) {
        _fotoUri.value = uri
        simularLectura()
    }

    fun simularLectura() {
        val productosFisicos = repositorio.obtenerProductosFisicos()
        val producto = productosFisicos[indiceSimulado % productosFisicos.size]
        indiceSimulado++
        _codigo.value = producto.codigoBarra
        _errorCodigo.value = null
        _productoDetectado.value = producto
        _mensaje.value = "Código leído (simulado): ${producto.codigoBarra}"
    }

    fun onCodigoLeido(texto: String) {
        _codigo.value = texto.trim().uppercase()
        buscarCodigo()
        if (_productoDetectado.value != null) {
            _mensaje.value = "Código leído con la cámara: ${_codigo.value}"
        }
    }

    fun onErrorEscaneo(mensaje: String) {
        _mensaje.value = mensaje
    }

    fun onCodigoChange(valor: String) {
        _codigo.value = valor.uppercase()
        _errorCodigo.value = null
    }

    fun buscarCodigo() {
        val error = Validaciones.validarCodigoProducto(_codigo.value)
        if (error != null) {
            _errorCodigo.value = error
            _productoDetectado.value = null
            _mensaje.value = null
            return
        }
        val producto = repositorio.buscarPorCodigo(_codigo.value)
        _productoDetectado.value = producto
        if (producto != null) {
            _errorCodigo.value = null
            _mensaje.value = "Producto encontrado: ${producto.codigoBarra}"
        } else {
            _errorCodigo.value = "No existe un producto con ese código"
            _mensaje.value = null
        }
    }
}
