package com.example.appmuseo.utils

import com.example.appmuseo.model.Cotizacion
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import kotlin.math.ceil

object CotizadorEnvio {

    const val PESO_MAXIMO_KG = 30.0
    const val MONTO_ENVIO_GRATIS = 50_000

    val zonas: List<Zona> = listOf(
        Zona("Región Metropolitana", TipoDestino.NACIONAL, 2_990, 990, 1, 2),
        Zona("Zona Centro (Valparaíso a Biobío)", TipoDestino.NACIONAL, 3_990, 1_290, 2, 4),
        Zona("Zona Norte (Arica a Coquimbo)", TipoDestino.NACIONAL, 4_990, 1_590, 3, 5),
        Zona("Zona Sur (La Araucanía a Los Lagos)", TipoDestino.NACIONAL, 4_990, 1_590, 3, 5),
        Zona("Zona Austral (Aysén y Magallanes)", TipoDestino.NACIONAL, 6_990, 2_490, 5, 8),
        Zona("Sudamérica", TipoDestino.INTERNACIONAL, 14_990, 5_990, 7, 12),
        Zona("Centroamérica, Caribe y México", TipoDestino.INTERNACIONAL, 17_990, 6_990, 10, 15),
        Zona("España y Portugal", TipoDestino.INTERNACIONAL, 21_990, 8_990, 10, 18)
    )

    fun zonasPorTipo(tipo: TipoDestino): List<Zona> = zonas.filter { it.tipo == tipo }

    fun calcularPesoFacturable(pesoKg: Double): Int = ceil(pesoKg).toInt().coerceAtLeast(1)

    fun cotizar(zona: Zona, pesoKg: Double, subtotalCompra: Int = 0): Cotizacion {
        require(pesoKg > 0) { "El peso debe ser mayor a 0 kg" }
        require(pesoKg <= PESO_MAXIMO_KG) { "El courier acepta como máximo ${PESO_MAXIMO_KG.toInt()} kg por envío" }

        val pesoFacturable = calcularPesoFacturable(pesoKg)
        val costoNormal = zona.tarifaBase + zona.costoPorKg * pesoFacturable
        val envioGratis = zona.tipo == TipoDestino.NACIONAL &&
                subtotalCompra >= MONTO_ENVIO_GRATIS

        return Cotizacion(
            zona = zona,
            pesoFacturable = pesoFacturable,
            costoEnvio = if (envioGratis) 0 else costoNormal,
            envioGratis = envioGratis
        )
    }
}
