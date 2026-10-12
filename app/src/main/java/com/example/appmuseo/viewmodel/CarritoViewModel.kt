package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * CARRITO compartido por varias pantallas (catálogo, detalle, escáner, carrito, solicitud).
 * Se crea UNA vez en AppNavigation y se pasa a cada pantalla (ViewModel compartido, Guía 11).
 */
class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items

    /**
     * Agrega una unidad. Devuelve false si no se pudo:
     *  - productos: no se puede superar el stock
     *  - cursos: solo se puede inscribir 1 vez
     */
    fun agregar(producto: Producto): Boolean {
        val itemActual = _items.value.find { it.producto.id == producto.id }
        val cantidadActual = itemActual?.cantidad ?: 0
        val limite = if (producto.categoria == Categoria.CURSO) 1 else producto.stock

        if (cantidadActual >= limite) return false

        _items.update { lista ->
            if (itemActual == null) {
                lista + ItemCarrito(producto, 1)                 // nuevo en el carrito
            } else {
                lista.map { item ->                              // ya estaba: sumamos 1
                    if (item.producto.id == producto.id) item.copy(cantidad = item.cantidad + 1) else item
                }
            }
        }
        return true
    }

    /** Resta una unidad; si queda en 0 se elimina del carrito (mapNotNull descarta los null). */
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

    // Cálculos del carrito (sumOf recorre la lista y suma)
    fun subtotal(): Int = _items.value.sumOf { it.subtotal }

    fun cantidadTotal(): Int = _items.value.sumOf { it.cantidad }

    fun pesoTotalKg(): Double = _items.value.sumOf { it.producto.pesoKg * it.cantidad }

    /** true si hay al menos un producto físico (los cursos no se despachan). */
    fun requiereEnvio(): Boolean = _items.value.any { it.producto.requiereEnvio }
}
