package com.example.appmuseo

import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.MapaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseosQrTest {

    @Test
    fun cadaMuseo_tieneSuCodigoQr() {
        val museo = MuseoRepository().buscarPorId(5)!!
        assertEquals("MUSEO-5", museo.codigoQr)
    }

    @Test
    fun repositorio_buscaPorCodigoSinImportarMayusculas() {
        assertEquals(1, MuseoRepository().buscarPorCodigo(" museo-1 ")?.id)
        assertNull(MuseoRepository().buscarPorCodigo("MUSEO-99"))
    }

    @Test
    fun validaciones_reconoceCodigoDeMuseo() {
        assertTrue(Validaciones.esCodigoMuseo("MUSEO-12"))
        assertFalse(Validaciones.esCodigoMuseo("MDM-001"))
    }

    @Test
    fun escaner_qrDeMuseo_encuentraElMuseoYNoUnProducto() {
        val vm = EscanerViewModel()
        vm.onCodigoLeido("MUSEO-10")

        assertEquals(10, vm.museoDetectado.value?.id)
        assertNull(vm.productoDetectado.value)
        assertNull(vm.errorCodigo.value)
    }

    @Test
    fun escaner_museoInexistente_muestraError() {
        val vm = EscanerViewModel()
        vm.onCodigoChange("MUSEO-77")
        vm.buscarCodigo()

        assertNull(vm.museoDetectado.value)
        assertNotNull(vm.errorCodigo.value)
    }

    @Test
    fun escaner_volverAEscanear_limpiaTodo() {
        val vm = EscanerViewModel()
        vm.simularLecturaMuseo(3)
        vm.reiniciar()

        assertNull(vm.museoDetectado.value)
        assertEquals("", vm.codigo.value)
    }

    @Test
    fun mapa_filtroProyecto_muestraSoloEseTipo() {
        val vm = MapaViewModel()
        vm.seleccionarTipo("Arqueología")

        val visibles = vm.estado.value.museosVisibles
        assertEquals(2, visibles.size)
        assertTrue(visibles.all { it.tipo == "Arqueología" })
    }

    @Test
    fun mapa_todos_limpiaPaisYProyecto() {
        val vm = MapaViewModel()
        vm.seleccionarPais("Chile")
        vm.seleccionarTipo("Arte")
        vm.limpiarFiltros()

        assertEquals(vm.estado.value.museos.size, vm.estado.value.museosVisibles.size)
    }
}
