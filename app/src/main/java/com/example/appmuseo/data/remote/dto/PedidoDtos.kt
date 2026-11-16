package com.example.appmuseo.data.remote.dto

// la app manda solo QUÉ compra y CUÁNTO; el servidor calcula precios, envío y totales
data class ItemPedidoDto(
    val productoId: Long,
    val cantidad: Int
)

data class PedidoRequestDto(
    val correoUsuario: String,
    val nombreCliente: String,
    val correoCliente: String,
    val direccion: String?,
    val zona: String?,
    val tipoEnvio: String?,   // ESTANDAR o EXPRESS
    val items: List<ItemPedidoDto>
)

data class LineaPedidoDto(
    val productoId: Long,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int,
    val total: Int
)

data class PedidoDto(
    val id: Long,
    val codigo: String,
    val correoUsuario: String,
    val nombreCliente: String,
    val correoCliente: String,
    val zonaEnvio: String?,
    val tipoEnvio: String?,
    val direccion: String?,
    val subtotal: Int,
    val costoEnvio: Int,
    val total: Int,
    val neto: Int,
    val iva: Int,
    val cantidadProductos: Int,
    val estado: String,
    val fecha: String?,
    val lineas: List<LineaPedidoDto>
)
