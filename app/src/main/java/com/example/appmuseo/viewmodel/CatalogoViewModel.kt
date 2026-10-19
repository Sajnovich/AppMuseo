package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.CatalogoUiState
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto
import com.example.appmuseo.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CatalogoViewModel(
    private val repositorio: CatalogoRepository = CatalogoRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState(productos = repositorio.obtenerCatalogo()))
    val estado: StateFlow<CatalogoUiState> = _estado

    fun seleccionarCategoria(categoria: Categoria?) {
        _estado.update { it.copy(categoriaSeleccionada = categoria) }
    }

    fun onBusquedaChange(texto: String) {
        _estado.update { it.copy(busqueda = texto) }
    }

    fun buscarPorId(id: Int): Producto? = repositorio.buscarPorId(id)

    fun destacados(): List<Producto> = _estado.value.productos.filter { it.destacado }
}
