package com.example.appmuseo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EscanerViewModel(
    private val repositorio: CatalogoRepository = CatalogoRepository(),
    private val museoRepositorio: MuseoRepository = MuseoRepository()
) : ViewModel() {

    private val _fotoUri = MutableStateFlow<Uri?>(null)
    val fotoUri: StateFlow<Uri?> = _fotoUri

    // Solo un ProductoFisico tiene código de barras, por eso
    private val _productoDetectado = MutableStateFlow<ProductoFisico?>(null)
    val productoDetectado: StateFlow<ProductoFisico?> = _productoDetectado

    // si el QR era de un museo (MUSEO-1, MUSEO-2...)
    private val _museoDetectado = MutableStateFlow<Museo?>(null)
    val museoDetectado: StateFlow<Museo?> = _museoDetectado

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
        _museoDetectado.value = null
        _productoDetectado.value = producto
        _mensaje.value = "Código leído (simulado): ${producto.codigoBarra}"
    }

    // para probar en el emulador sin imprimir el QR
    fun simularLecturaMuseo(id: Int = 1) {
        onCodigoLeido("MUSEO-$id")
    }

    fun onCodigoLeido(texto: String) {
        _codigo.value = texto.trim().uppercase()
        buscarCodigo()
        if (_productoDetectado.value != null || _museoDetectado.value != null) {
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

    // boton "Volver a escanear"
    fun reiniciar() {
        _codigo.value = ""
        _errorCodigo.value = null
        _productoDetectado.value = null
        _museoDetectado.value = null
        _mensaje.value = null
        _fotoUri.value = null
    }

    fun buscarCodigo() {
        if (Validaciones.esCodigoMuseo(_codigo.value)) {
            val museo = museoRepositorio.buscarPorCodigo(_codigo.value)
            _museoDetectado.value = museo
            _productoDetectado.value = null
            if (museo != null) {
                _errorCodigo.value = null
                _mensaje.value = "Museo encontrado: ${museo.codigoQr}"
            } else {
                _errorCodigo.value = "No existe un museo con ese código"
                _mensaje.value = null
            }
            return
        }
        _museoDetectado.value = null
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
