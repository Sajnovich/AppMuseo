package com.example.appmuseo

import com.example.appmuseo.model.TipoNotificacion
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificacionesFavoritosTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun notificaciones_marcarTodas_dejaCeroSinLeer() {
        val vm = NotificacionesViewModel()
        assertTrue(vm.cantidadNoLeidas() > 0)

        vm.marcarTodasLeidas()

        assertEquals(0, vm.cantidadNoLeidas())
    }

    @Test
    fun notificaciones_agregar_quedaPrimeraYSinLeer() {
        val vm = NotificacionesViewModel()
        val antes = vm.cantidadNoLeidas()

        vm.agregar("Solicitud enviada", "Tu pedido va en camino", TipoNotificacion.SOLICITUD)

        assertEquals("Solicitud enviada", vm.notificaciones.value.first().titulo)
        assertEquals(antes + 1, vm.cantidadNoLeidas())
    }

    @Test
    fun notificaciones_eliminar_laSacaDeLaLista() {
        val vm = NotificacionesViewModel()
        val id = vm.notificaciones.value.first().id

        vm.eliminar(id)

        assertTrue(vm.notificaciones.value.none { it.id == id })
    }

    @Test
    fun favoritos_alternarDosVeces_loAgregaYLoQuita() {
        val vm = FavoritosViewModel()

        assertTrue(vm.alternarProducto(5))
        assertEquals(1, vm.total())
        assertFalse(vm.alternarProducto(5))
        assertEquals(0, vm.total())
    }

    @Test
    fun favoritos_museosYProductosSeGuardanAparte() {
        val vm = FavoritosViewModel()
        vm.alternarMuseo(1)
        vm.alternarProducto(1)

        assertEquals(setOf(1), vm.museos.value)
        assertEquals(setOf(1), vm.productos.value)
        assertEquals(2, vm.total())
    }
}
