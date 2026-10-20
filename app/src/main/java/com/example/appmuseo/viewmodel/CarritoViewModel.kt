package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.Cotizacion
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.OpcionesEnvio
import com.example.appmuseo.model.ResumenCompra
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.TipoEnvio
import com.example.appmuseo.model.Zona
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items

    private val _envio = MutableStateFlow(OpcionesEnvio())
    val envio: StateFlow<OpcionesEnvio> = _envio

    // cuantas unidades mas se pueden agregar sin pasar el stock o el cupo
    fun disponibleParaAgregar(producto: Producto): Int {
        val enCarrito = _items.value.find { it.producto.id == producto.id }?.cantidad ?: 0
        return (producto.limiteCompra() - enCarrito).coerceAtLeast(0)
    }

    // agrega varias unidades de una vez (detalle del producto). Devuelve cuantas entraron
    fun agregarCantidad(producto: Producto, cantidad: Int): Int {
        var agregadas = 0
        repeat(cantidad) { if (agregar(producto)) agregadas++ }
        return agregadas
    }

    fun onTipoDestinoChange(tipo: TipoDestino) {
        _envio.update { it.copy(tipoDestino = tipo, zona = null) }
    }

    fun onZonaChange(zona: Zona) {
        _envio.update { it.copy(zona = zona, tipoDestino = zona.tipo) }
    }

    fun onTipoEnvioChange(tipo: TipoEnvio) {
        _envio.update { it.copy(tipoEnvio = tipo) }
    }

    // se recalcula con lo que haya en el carrito en ese momento
    fun resumen(): ResumenCompra {
        val items = _items.value
        val opciones = _envio.value
        val zona = opciones.zona
        val peso = items.sumOf { it.producto.pesoKg * it.cantidad }
        if (zona == null || peso <= 0.0) return ResumenCompra(items)
        return try {
            val cotizacion: Cotizacion = CotizadorEnvio.cotizar(zona, peso, items.sumOf { it.subtotal }, opciones.tipoEnvio)
            ResumenCompra(items, cotizacion)
        } catch (e: IllegalArgumentException) {
            ResumenCompra(items, errorEnvio = e.message)
        }
    }

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
        _envio.value = OpcionesEnvio()
    }

    fun subtotal(): Int = _items.value.sumOf { it.subtotal }

    fun cantidadTotal(): Int = _items.value.sumOf { it.cantidad }

    fun pesoTotalKg(): Double = _items.value.sumOf { it.producto.pesoKg * it.cantidad }

    fun requiereEnvio(): Boolean = _items.value.any { it.producto.requiereEnvio }
}
