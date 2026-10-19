package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items

    fun agregar(producto: Producto): Boolean {
        val itemActual = _items.value.find { it.producto.id == producto.id }
        val cantidadActual = itemActual?.cantidad ?: 0
        // polimorfismo
        val limite = producto.limiteCompra()

        if (cantidadActual >= limite) return false

        _items.update { lista ->
            if (itemActual == null) {
                lista + ItemCarrito(producto, 1)
            } else {
                lista.map { item ->
                    if (item.producto.id == producto.id) item.copy(cantidad = item.cantidad + 1) else item
                }
            }
        }
        return true
    }

    fun disminuir(productoId: Int) {
        _items.update { lista ->
            lista.mapNotNull { item ->
                when {
                    item.producto.id != productoId -> item
                    item.cantidad > 1 -> item.copy(cantidad = item.cantidad - 1)
                    else -> null
                }
            }
        }
    }

    fun eliminar(productoId: Int) {
        _items.update { lista -> lista.filter { it.producto.id != productoId } }
    }

    fun vaciar() {
        _items.value = emptyList()
    }

    fun subtotal(): Int = _items.value.sumOf { it.subtotal }

    fun cantidadTotal(): Int = _items.value.sumOf { it.cantidad }

    fun pesoTotalKg(): Double = _items.value.sumOf { it.producto.pesoKg * it.cantidad }

    fun requiereEnvio(): Boolean = _items.value.any { it.producto.requiereEnvio }
}
