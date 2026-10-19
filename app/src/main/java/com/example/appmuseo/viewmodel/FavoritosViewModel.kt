package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Guarda solo los ids, los datos completos se sacan de los repositorios
class FavoritosViewModel : ViewModel() {

    private val _museos = MutableStateFlow<Set<Int>>(emptySet())
    val museos: StateFlow<Set<Int>> = _museos

    private val _productos = MutableStateFlow<Set<Int>>(emptySet())
    val productos: StateFlow<Set<Int>> = _productos

    // si ya estaba lo saca, si no lo agrega. Devuelve true si quedo guardado
    fun alternarMuseo(id: Int): Boolean {
        _museos.update { if (id in it) it - id else it + id }
        return id in _museos.value
    }

    fun alternarProducto(id: Int): Boolean {
        _productos.update { if (id in it) it - id else it + id }
        return id in _productos.value
    }

    fun total(): Int = _museos.value.size + _productos.value.size
}
