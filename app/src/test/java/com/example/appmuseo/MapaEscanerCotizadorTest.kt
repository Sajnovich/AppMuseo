package com.example.appmuseo

import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.Distancia
import com.example.appmuseo.utils.ProyeccionMapa
import com.example.appmuseo.utils.formatearDistancia
import com.example.appmuseo.viewmodel.CotizadorViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.MapaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del mapa (incluye museo más cercano), el escáner simulado, el cotizador (ViewModel) y las rutas. */
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

    // ── Distancia y museo más cercano (ubicación) ──

    @Test
    fun distancia_entreUnPuntoYElMismo_esCero() {
        assertEquals(0.0, Distancia.kilometrosEntre(-33.45, -70.66, -33.45, -70.66), 0.001)
    }

    @Test
    fun distancia_santiagoValparaiso_rondaLos100Km() {
        val km = Distancia.kilometrosEntre(-33.45, -70.66, -33.05, -71.62)
        assertTrue("Se obtuvo $km km", km in 90.0..110.0)
    }

    @Test
    fun museoMasCercano_desdeLima_esElMuseoAndino() {
        val museos = MuseoRepository().obtenerMuseos()

        val cercano = Distancia.museoMasCercano(museos, -12.06, -77.03)

        assertEquals(5, cercano?.id)
    }

    @Test
    fun museoMasCercano_listaVacia_devuelveNull() {
        assertNull(Distancia.museoMasCercano(emptyList(), 0.0, 0.0))
    }

    @Test
    fun mapa_buscarCercanoConUbicacionReal_seleccionaElMuseoYQuitaElFiltro() {
        val viewModel = MapaViewModel()
        viewModel.seleccionarPais("Chile")

        viewModel.buscarMuseoCercano(40.40, -3.68)   // cerca de Madrid

        val estado = viewModel.estado.value
        assertEquals(13, estado.museoSeleccionado?.id)
        assertNull(estado.paisSeleccionado)           // el filtro se quita para ver el museo en el mapa
        assertFalse(estado.cercano!!.ubicacionAproximada)
    }

    @Test
    fun mapa_buscarCercanoSinUbicacion_usaLaReferenciaYLoIndica() {
        val viewModel = MapaViewModel()

        viewModel.buscarMuseoCercano(null, null)      // sin permiso o sin señal

        val cercano = viewModel.estado.value.cercano
        assertNotNull(cercano)
        assertTrue(cercano!!.ubicacionAproximada)
        assertEquals(1, cercano.museo.id)             // el de Santiago, a pocos km de la referencia
        assertTrue(cercano.distanciaKm < 5.0)
    }

    @Test
    fun formatoDistancia_redondeaYSeparaMiles() {
        assertEquals("menos de 1 km", formatearDistancia(0.4))
        assertEquals("1.235 km", formatearDistancia(1234.6))
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

        viewModel.onCodigoChange("mdm-005")
        viewModel.buscarCodigo()

        assertEquals(5, viewModel.productoDetectado.value?.id)
        assertNull(viewModel.errorCodigo.value)
    }

    @Test
    fun escaner_codigoInexistente_noEncuentraNada() {
        val viewModel = EscanerViewModel()

        viewModel.onCodigoChange("MDM-999")
        viewModel.buscarCodigo()

        assertNull(viewModel.productoDetectado.value)
        assertNotNull(viewModel.errorCodigo.value)
    }

    @Test
    fun escaner_codigoConFormatoInvalido_muestraErrorDelCampo() {
        val viewModel = EscanerViewModel()

        viewModel.onCodigoChange("XYZ")
        viewModel.buscarCodigo()

        assertEquals("Formato esperado: MDM-001", viewModel.errorCodigo.value)
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

        assertNotNull(viewModel.estado.value.errores.zona)
        assertNull(viewModel.estado.value.errores.peso)   // el peso sí estaba bien
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

        assertNotNull(viewModel.estado.value.errores.peso)
    }

    // ── Navegación ──

    @Test
    fun rutaDetalle_reemplazaElArgumento() {
        assertEquals("detalle/7", Screen.Detalle(7).construirRuta())
        assertEquals("home", Screen.Home.construirRuta())
    }
}
