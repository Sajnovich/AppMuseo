package com.example.appmuseo.utils

import com.example.appmuseo.model.Cotizacion
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import kotlin.math.ceil

/**
 * MÓDULO AUTÓNOMO DE COTIZACIÓN (reglas algorítmicas simuladas del courier).
 *
 * Reglas de negocio:
 *  1. El peso debe ser mayor a 0 y como máximo 30 kg (si no, se lanza un error).
 *  2. Peso facturable = peso redondeado hacia ARRIBA, con un mínimo de 1 kg.
 *     (1,2 kg se cobra como 2 kg; 0,3 kg se cobra como 1 kg).
 *  3. Costo = tarifa base de la zona + (costo por kg × peso facturable).
 *  4. Envío GRATIS solo para destinos nacionales con compras desde $50.000.
 *
 * No usa Android ni Internet: por eso se prueba fácil con pruebas unitarias.
 */
object CotizadorEnvio {

    const val PESO_MAXIMO_KG = 30.0
    const val MONTO_ENVIO_GRATIS = 50_000

    // Tabla de zonas (en la Unidad 3 podría venir desde el backend)
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

    /** Regla 2: redondeo hacia arriba con mínimo 1 kg. */
    fun calcularPesoFacturable(pesoKg: Double): Int = ceil(pesoKg).toInt().coerceAtLeast(1)

    /**
     * Calcula el costo del envío.
     * @throws IllegalArgumentException si el peso no es válido (control de errores, Guía 4).
     */
    fun cotizar(zona: Zona, pesoKg: Double, subtotalCompra: Int = 0): Cotizacion {
        // Regla 1: "require" lanza IllegalArgumentException con el mensaje si la condición es falsa
        require(pesoKg > 0) { "El peso debe ser mayor a 0 kg" }
        require(pesoKg <= PESO_MAXIMO_KG) { "El courier acepta como máximo ${PESO_MAXIMO_KG.toInt()} kg por envío" }

        val pesoFacturable = calcularPesoFacturable(pesoKg)                     // Regla 2
        val costoNormal = zona.tarifaBase + zona.costoPorKg * pesoFacturable     // Regla 3
        val envioGratis = zona.tipo == TipoDestino.NACIONAL &&                   // Regla 4
                subtotalCompra >= MONTO_ENVIO_GRATIS

        return Cotizacion(
            zona = zona,
            pesoFacturable = pesoFacturable,
            costoEnvio = if (envioGratis) 0 else costoNormal,
            envioGratis = envioGratis
        )
    }
}
