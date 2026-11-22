package com.example.appmuseo

import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.TipoEnvio
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.viewmodel.CarritoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CompraEnvioTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalogo = CatalogoRepository()
    private val taza = catalogo.buscarPorId(1)!!          // $8.990, 0,4 kg
    private val curso = catalogo.buscarPorId(10)!!        // $89.990, sin envio
    private val rm = CotizadorEnvio.zonas.first { it.nombre == "Región Metropolitana" }
    private val sudamerica = CotizadorEnvio.zonas.first { it.nombre == "Sudamérica" }

    @Test
    fun resumen_totalEsSubtotalMasEnvio() {
        val vm = CarritoViewModel()
        vm.agregarCantidad(taza, 2)
        vm.onZonaChange(rm)

        val r = vm.resumen()
        assertEquals(17_980, r.subtotal)
        assertEquals(2_990 + 990, r.costoEnvio)   // 0,8 kg -> 1 kg facturable
        assertEquals(r.subtotal + r.costoEnvio, r.total)
        assertTrue(r.listoParaPagar)
    }

    @Test
    fun resumen_sinZona_noEstaListoParaPagar() {
        val vm = CarritoViewModel()
        vm.agregar(taza)

        assertFalse(vm.resumen().listoParaPagar)
    }

    @Test
    fun resumen_soloCursos_noNecesitaEnvio() {
        val vm = CarritoViewModel()
        vm.agregar(curso)

        val r = vm.resumen()
        assertFalse(r.requiereEnvio)
        assertEquals(0, r.costoEnvio)
        assertTrue(r.listoParaPagar)
    }

    @Test
    fun express_cuestaMasYLlegaAntes() {
        val estandar = CotizadorEnvio.cotizar(sudamerica, 1.0)
        val express = CotizadorEnvio.cotizar(sudamerica, 1.0, tipoEnvio = TipoEnvio.EXPRESS)

        assertTrue(express.costoEnvio > estandar.costoEnvio)
        assertTrue(express.diasMax < estandar.diasMax)
    }

    @Test
    fun express_noTieneEnvioGratis() {
        val c = CotizadorEnvio.cotizar(rm, 1.0, subtotalCompra = 60_000, tipoEnvio = TipoEnvio.EXPRESS)
        assertFalse(c.envioGratis)
        assertTrue(c.costoEnvio > 0)
    }

    @Test
    fun agregarCantidad_noPasaElStock() {
        val vm = CarritoViewModel()
        val guantes = catalogo.buscarPorId(6)!!   // stock 8

        val agregadas = vm.agregarCantidad(guantes, 20)

        assertEquals(8, agregadas)
        assertEquals(0, vm.disponibleParaAgregar(guantes))
    }

    @Test
    fun cambiarANacional_borraLaZonaInternacional() {
        val vm = CarritoViewModel()
        vm.agregar(taza)
        vm.onZonaChange(sudamerica)
        vm.onTipoDestinoChange(TipoDestino.NACIONAL)

        assertEquals(null, vm.envio.value.zona)
    }

    @Test
    fun faltaParaEnvioGratis_seCalculaBien() {
        val vm = CarritoViewModel()
        vm.agregar(taza)
        assertEquals(CotizadorEnvio.MONTO_ENVIO_GRATIS - 8_990, vm.resumen().faltaParaEnvioGratis)
        assertNotNull(vm.resumen())
    }
}
