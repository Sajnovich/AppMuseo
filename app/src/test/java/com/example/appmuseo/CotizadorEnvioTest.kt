package com.example.appmuseo

import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.utils.CotizadorEnvio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas algorítmicas del courier simulado. */
class CotizadorEnvioTest {

    private val regionMetropolitana = CotizadorEnvio.zonas.first { it.nombre == "Región Metropolitana" }
    private val sudamerica = CotizadorEnvio.zonas.first { it.nombre == "Sudamérica" }

    @Test
    fun pesoFacturable_seRedondeaHaciaArriba_conMinimoUnKilo() {
        assertEquals(1, CotizadorEnvio.calcularPesoFacturable(0.3))
        assertEquals(2, CotizadorEnvio.calcularPesoFacturable(1.2))
        assertEquals(3, CotizadorEnvio.calcularPesoFacturable(3.0))
    }

    @Test
    fun envioNacional_calculaTarifaBaseMasCostoPorKilo() {
        // 1,2 kg → se cobran 2 kg → 2.990 + 990 × 2 = 4.970
        val cotizacion = CotizadorEnvio.cotizar(regionMetropolitana, 1.2)

        assertEquals(4_970, cotizacion.costoEnvio)
        assertEquals(2, cotizacion.pesoFacturable)
        assertFalse(cotizacion.envioGratis)
    }

    @Test
    fun envioNacional_conCompraSobreElMinimo_esGratis() {
        val cotizacion = CotizadorEnvio.cotizar(regionMetropolitana, 2.0, subtotalCompra = 60_000)

        assertTrue(cotizacion.envioGratis)
        assertEquals(0, cotizacion.costoEnvio)
    }

    @Test
    fun envioInternacional_nuncaEsGratis() {
        // 14.990 + 5.990 × 2 = 26.970
        val cotizacion = CotizadorEnvio.cotizar(sudamerica, 2.0, subtotalCompra = 60_000)

        assertFalse(cotizacion.envioGratis)
        assertEquals(26_970, cotizacion.costoEnvio)
    }

    @Test
    fun pesoCeroONegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException::class.java) {
            CotizadorEnvio.cotizar(regionMetropolitana, 0.0)
        }
    }

    @Test
    fun pesoSobreElMaximo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException::class.java) {
            CotizadorEnvio.cotizar(regionMetropolitana, 31.0)
        }
    }

    @Test
    fun zonasPorTipo_separaNacionalesEInternacionales() {
        val nacionales = CotizadorEnvio.zonasPorTipo(TipoDestino.NACIONAL)
        val internacionales = CotizadorEnvio.zonasPorTipo(TipoDestino.INTERNACIONAL)

        assertTrue(nacionales.all { it.tipo == TipoDestino.NACIONAL })
        assertEquals(CotizadorEnvio.zonas.size, nacionales.size + internacionales.size)
    }
}
