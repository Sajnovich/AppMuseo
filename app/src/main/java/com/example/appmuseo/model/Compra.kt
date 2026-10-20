package com.example.appmuseo.model

import com.example.appmuseo.utils.CotizadorEnvio

// lo que el usuario elige en el carrito para el despacho
data class OpcionesEnvio(
    val tipoDestino: TipoDestino = TipoDestino.NACIONAL,
    val zona: Zona? = null,
    val tipoEnvio: TipoEnvio = TipoEnvio.ESTANDAR
)

// todos los numeros de la compra calculados en un solo lugar
data class ResumenCompra(
    val items: List<ItemCarrito>,
    val cotizacion: Cotizacion? = null,
    val errorEnvio: String? = null
) {
    val subtotal: Int get() = items.sumOf { it.subtotal }
    val cantidadProductos: Int get() = items.sumOf { it.cantidad }
    val requiereEnvio: Boolean get() = items.any { it.producto.requiereEnvio }
    val pesoKg: Double get() = items.sumOf { it.producto.pesoKg * it.cantidad }
    val costoEnvio: Int get() = if (requiereEnvio) cotizacion?.costoEnvio ?: 0 else 0
    val total: Int get() = subtotal + costoEnvio

    // falta elegir destino si hay productos fisicos
    val listoParaPagar: Boolean
        get() = items.isNotEmpty() && (!requiereEnvio || cotizacion != null)

    // cuanto falta para el envio gratis (0 si ya se cumple)
    val faltaParaEnvioGratis: Int
        get() = (CotizadorEnvio.MONTO_ENVIO_GRATIS - subtotal).coerceAtLeast(0)
}
