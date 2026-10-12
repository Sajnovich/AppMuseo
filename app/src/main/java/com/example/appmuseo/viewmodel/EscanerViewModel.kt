package com.example.appmuseo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.Producto
import com.example.appmuseo.repository.CatalogoRepository
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

    private val _productoDetectado = MutableStateFlow<Producto?>(null)
    val productoDetectado: StateFlow<Producto?> = _productoDetectado

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private var indiceSimulado = 0

    fun onFotoCapturada(uri: Uri) {
        _fotoUri.value = uri
        simularLectura()
    }

    /** "Lee" el siguiente producto físico del catálogo (los cursos no tienen código impreso). */
    fun simularLectura() {
        val productosFisicos = repositorio.obtenerCatalogo().filter { it.requiereEnvio }
        val producto = productosFisicos[indiceSimulado % productosFisicos.size]
        indiceSimulado++
        _productoDetectado.value = producto
        _mensaje.value = "Código leído (simulado): ${producto.codigoBarra}"
    }

    /** Alternativa accesible: escribir el código a mano. */
    fun buscarCodigo(codigo: String) {
        if (codigo.isBlank()) {
            _mensaje.value = "Escribe un código, por ejemplo MDM-001"
            return
        }
        val producto = repositorio.buscarPorCodigo(codigo)
        _productoDetectado.value = producto
        _mensaje.value = if (producto != null) {
            "Producto encontrado: ${producto.codigoBarra}"
        } else {
            "No existe un producto con el código ${codigo.trim().uppercase()}"
        }
    }
}
