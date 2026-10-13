package com.example.appmuseo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ESCÁNER SIMULADO (el caso pide "simulación del uso de la cámara para lectura de códigos").
 * La foto se toma con la cámara real, pero la "lectura" del código se simula:
 * como no tenemos un lector de códigos de barra, recorremos el catálogo en orden.
 */
class EscanerViewModel(
    private val repositorio: CatalogoRepository = CatalogoRepository()
) : ViewModel() {

    private val _fotoUri = MutableStateFlow<Uri?>(null)
    val fotoUri: StateFlow<Uri?> = _fotoUri

    // Solo un ProductoFisico tiene código de barras, por eso el tipo es esa subclase
    private val _productoDetectado = MutableStateFlow<ProductoFisico?>(null)
    val productoDetectado: StateFlow<ProductoFisico?> = _productoDetectado

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // Campo "código manual" y su error (validación por campo, igual que los demás formularios)
    private val _codigo = MutableStateFlow("")
    val codigo: StateFlow<String> = _codigo

    private val _errorCodigo = MutableStateFlow<String?>(null)
    val errorCodigo: StateFlow<String?> = _errorCodigo

    private var indiceSimulado = 0

    fun onFotoCapturada(uri: Uri) {
        _fotoUri.value = uri
        simularLectura()
    }

    /** "Lee" el siguiente producto físico del catálogo (los cursos no tienen código impreso). */
    fun simularLectura() {
        val productosFisicos = repositorio.obtenerProductosFisicos()
        val producto = productosFisicos[indiceSimulado % productosFisicos.size]
        indiceSimulado++
        _codigo.value = producto.codigoBarra   // el código "leído" aparece en el campo
        _errorCodigo.value = null
        _productoDetectado.value = producto
        _mensaje.value = "Código leído (simulado): ${producto.codigoBarra}"
    }

    fun onCodigoChange(valor: String) {
        _codigo.value = valor.uppercase()
        _errorCodigo.value = null
    }

    /** Alternativa accesible: escribir el código a mano. */
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
