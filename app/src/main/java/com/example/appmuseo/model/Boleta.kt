package com.example.appmuseo.model

import kotlin.math.roundToInt

// una linea de la boleta: cuantos, que producto y a que precio se compro
data class LineaBoleta(
    val productoId: Int,
    val cantidad: Int,
    val nombre: String,
    val precioUnitario: Int
) {
    val total: Int get() = cantidad * precioUnitario
}

object Boleta {

    private const val SEP_CAMPO = "¦"
    private const val SEP_LINEA = "\n"
    private const val IVA = 0.19

    // Room guarda texto, asi que las lineas se juntan en un solo String:
    // "1¦2¦Taza Museo¦8990\n7¦1¦Manual¦22990"
    fun codificar(lineas: List<LineaBoleta>): String =
        lineas.joinToString(SEP_LINEA) { listOf(it.productoId, it.cantidad, it.nombre.replace(SEP_CAMPO, " "), it.precioUnitario).joinToString(SEP_CAMPO) }

    fun decodificar(texto: String): List<LineaBoleta> =
        texto.split(SEP_LINEA).mapNotNull { linea ->
            val partes = linea.split(SEP_CAMPO)
            if (partes.size != 4) return@mapNotNull null
            val id = partes[0].toIntOrNull() ?: return@mapNotNull null
            val cantidad = partes[1].toIntOrNull() ?: return@mapNotNull null
            val precio = partes[3].toIntOrNull() ?: return@mapNotNull null
            LineaBoleta(id, cantidad, partes[2], precio)
        }

    fun desdeCarrito(items: List<ItemCarrito>): List<LineaBoleta> =
        items.map { LineaBoleta(it.producto.id, it.cantidad, it.producto.nombre, it.producto.precio) }

    // en Chile los precios ya incluyen IVA: neto = total / 1,19
    fun neto(total: Int): Int = (total / (1 + IVA)).roundToInt()

    fun iva(total: Int): Int = total - neto(total)
}
