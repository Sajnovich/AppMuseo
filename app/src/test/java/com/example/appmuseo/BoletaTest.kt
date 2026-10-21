package com.example.appmuseo

import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.model.Boleta
import com.example.appmuseo.model.LineaBoleta
import com.example.appmuseo.utils.textoBoleta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoletaTest {

    private val lineas = listOf(
        LineaBoleta(1, 2, "Taza Museo de los Museos", 8_990),
        LineaBoleta(8, 1, "Guía de Mediación Cultural", 15_990)
    )

    @Test
    fun codificarYDecodificar_devuelveLasMismasLineas() {
        assertEquals(lineas, Boleta.decodificar(Boleta.codificar(lineas)))
    }

    @Test
    fun decodificar_textoVacio_noRevienta() {
        assertTrue(Boleta.decodificar("").isEmpty())
    }

    @Test
    fun lineaBoleta_totalEsCantidadPorPrecio() {
        assertEquals(17_980, lineas[0].total)
    }

    @Test
    fun netoMasIva_esElTotal() {
        val total = 39_950
        assertEquals(33_571, Boleta.neto(total))
        assertEquals(total, Boleta.neto(total) + Boleta.iva(total))
    }

    @Test
    fun textoBoleta_traeProductosYTotal() {
        val solicitud = SolicitudEntity(
            id = 12, tipo = "COMPRA", detalle = "", lineas = Boleta.codificar(lineas),
            nombreCliente = "Ana", correoCliente = "ana@correo.cl", zonaEnvio = "", direccion = "",
            subtotal = 33_970, costoEnvio = 5_980, total = 39_950
        )
        val texto = textoBoleta(solicitud)
        assertTrue(texto.contains("2 x Taza Museo de los Museos"))
        assertTrue(texto.contains("TOTAL: $39.950"))
        assertTrue(texto.contains("2026-00012"))
    }
}
