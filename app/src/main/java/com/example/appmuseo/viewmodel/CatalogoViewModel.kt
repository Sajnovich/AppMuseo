package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.model.EstadoRed
import kotlinx.coroutines.launch
import com.example.appmuseo.model.CatalogoUiState
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto
import com.example.appmuseo.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CatalogoViewModel(
    private val repositorio: CatalogoRepository = CatalogoRepository(RetrofitInstance.catalogo)
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState(productos = repositorio.obtenerCatalogo()))
    val estado: StateFlow<CatalogoUiState> = _estado

    // Pide los datos a ms-catalogo con Retrofit. Si el servidor no responde se sigue con la copia local
    fun sincronizar() {
        if (_estado.value.red.cargando) return
        _estado.update { it.copy(red = it.red.copy(cargando = true, aviso = null)) }
        viewModelScope.launch {
            val resultado = repositorio.sincronizar()
            _estado.update {
                it.copy(
                    productos = repositorio.obtenerCatalogo(),
                    red = EstadoRed(
                        cargando = false,
                        desdeServidor = resultado is ResultadoApi.Exito,
                        aviso = when (resultado) {
                            is ResultadoApi.Exito -> null
                            is ResultadoApi.ErrorServidor -> resultado.mensaje
                            is ResultadoApi.SinConexion -> "Sin conexión con el servidor: mostrando datos guardados"
                        }
                    )
                )
            }
        }
    }

    fun seleccionarCategoria(categoria: Categoria?) {
        _estado.update { it.copy(categoriaSeleccionada = categoria) }
    }

    fun onBusquedaChange(texto: String) {
        _estado.update { it.copy(busqueda = texto) }
    }

    fun buscarPorId(id: Int): Producto? = repositorio.buscarPorId(id)

    fun destacados(): List<Producto> = _estado.value.productos.filter { it.destacado }
}
