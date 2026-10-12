package com.example.appmuseo

import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.ProyeccionMapa
import com.example.appmuseo.viewmodel.CotizadorViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.MapaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del mapa, el escáner simulado, el cotizador (ViewModel) y las rutas. */
class MapaEscanerCotizadorTest {

    // ── Mapa ──

    @Test
    fun proyeccion_siempreQuedaDentroDelMapa() {
        val (x, y) = ProyeccionMapa.aPosicionRelativa(-33.44, -70.65)   // Santiago
        assertTrue(x in 0f..1f && y in 0f..1f)
    }

    @Test
    fun proyeccion_madridQuedaAlEsteYAlNorteDeSantiago() {
        val santiago = ProyeccionMapa.aPosicionRelativa(-33.44, -70.65)
        val madrid = ProyeccionMapa.aPosicionRelativa(40.42, -3.70)

        assertTrue(madrid.first > santiago.first)    // más a la derecha
        assertTrue(madrid.second < santiago.second)  // más arriba
    }

    @Test
    fun mapa_filtrarPorPais_muestraSoloEsePais() {
        val viewModel = MapaViewModel()

        viewModel.seleccionarPais("Chile")

        val visibles = viewModel.estado.value.museosVisibles
        assertTrue(visibles.isNotEmpty())
        assertTrue(visibles.all { it.pais == "Chile" })
    }

    @Test
    fun mapa_alCambiarDePais_seQuitaElMuseoDeOtroPais() {
        val viewModel = MapaViewModel()
        val museoChileno = viewModel.estado.value.museos.first { it.pais == "Chile" }
        viewModel.seleccionarMuseo(museoChileno)

        viewModel.seleccionarPais("México")

        assertNull(viewModel.estado.value.museoSeleccionado)
    }

    // ── Escáner ──

    @Test
    fun escaner_codigoExistente_encuentraProducto() {
        val viewModel = EscanerViewModel()

        viewModel.buscarCodigo("mdm-005")

        assertEquals(5, viewModel.productoDetectado.value?.id)
    }

    @Test
    fun escaner_codigoInexistente_noEncuentraNada() {
        val viewModel = EscanerViewModel()

        viewModel.buscarCodigo("XYZ-999")

        assertNull(viewModel.productoDetectado.value)
        assertNotNull(viewModel.mensaje.value)
    }

    @Test
    fun escaner_lecturaSimulada_nuncaDevuelveUnCurso() {
        val viewModel = EscanerViewModel()

        repeat(20) {
            viewModel.simularLectura()
            assertTrue(viewModel.productoDetectado.value!!.requiereEnvio)
        }
    }

    // ── Cotizador (ViewModel) ──

    @Test
    fun cotizador_sinZona_muestraError() {
        val viewModel = CotizadorViewModel()
        viewModel.onPesoChange("2")

        viewModel.calcular()

        assertNotNull(viewModel.estado.value.error)
        assertNull(viewModel.estado.value.resultado)
    }

    @Test
    fun cotizador_pesoConComa_seInterpretaCorrectamente() {
        val viewModel = CotizadorViewModel()
        viewModel.onTipoDestinoChange(TipoDestino.NACIONAL)
        viewModel.onZonaChange(CotizadorEnvio.zonasPorTipo(TipoDestino.NACIONAL).first())
        viewModel.onPesoChange("1,5")

        viewModel.calcular()

        assertEquals(2, viewModel.estado.value.resultado?.pesoFacturable)
    }

    @Test
    fun cotizador_pesoNoNumerico_muestraError() {
        val viewModel = CotizadorViewModel()
        viewModel.onZonaChange(CotizadorEnvio.zonas.first())
        viewModel.onPesoChange("abc")

        viewModel.calcular()

        assertNotNull(viewModel.estado.value.error)
    }

    // ── Navegación ──

    @Test
    fun rutaDetalle_reemplazaElArgumento() {
        assertEquals("detalle/7", Screen.Detalle(7).construirRuta())
        assertEquals("home", Screen.Home.construirRuta())
    }
}
