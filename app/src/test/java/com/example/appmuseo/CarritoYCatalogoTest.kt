package com.example.appmuseo

import com.example.appmuseo.model.Categoria
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del catálogo (filtros) y del carrito (cantidades, stock y totales). */
class CarritoYCatalogoTest {

    private val repositorio = CatalogoRepository()
    private val taza = repositorio.buscarPorId(1)!!      // Merch, $8.990, 0,4 kg
    private val curso = repositorio.buscarPorId(10)!!    // Curso, $89.990

    // ── Catálogo ──

    @Test
    fun catalogo_filtraPorCategoria() {
        val viewModel = CatalogoViewModel(repositorio)

        viewModel.seleccionarCategoria(Categoria.CURSO)

        val visibles = viewModel.estado.value.productosFiltrados
        assertTrue(visibles.isNotEmpty())
        assertTrue(visibles.all { it.categoria == Categoria.CURSO })
    }

    @Test
    fun catalogo_buscaPorTextoSinImportarMayusculas() {
        val viewModel = CatalogoViewModel(repositorio)

        viewModel.onBusquedaChange("TAZA")

        assertEquals(listOf(taza), viewModel.estado.value.productosFiltrados)
    }

    @Test
    fun catalogo_sinFiltros_muestraTodo() {
        val viewModel = CatalogoViewModel(repositorio)

        viewModel.seleccionarCategoria(null)

        assertEquals(repositorio.obtenerCatalogo().size, viewModel.estado.value.productosFiltrados.size)
    }

    // ── Carrito ──

    @Test
    fun carrito_agregarDosVeces_sumaCantidadYSubtotal() {
        val carrito = CarritoViewModel()

        carrito.agregar(taza)
        carrito.agregar(taza)

        assertEquals(1, carrito.items.value.size)       // una sola línea
        assertEquals(2, carrito.cantidadTotal())
        assertEquals(17_980, carrito.subtotal())
    }

    @Test
    fun carrito_cursoSoloSePuedeAgregarUnaVez() {
        val carrito = CarritoViewModel()

        assertTrue(carrito.agregar(curso))
        assertFalse(carrito.agregar(curso))
    }

    @Test
    fun carrito_noSuperaElStock() {
        val carrito = CarritoViewModel()
        val cajaConservacion = repositorio.buscarPorId(6)!!   // stock 8

        repeat(cajaConservacion.stock) { carrito.agregar(cajaConservacion) }

        assertFalse(carrito.agregar(cajaConservacion))
        assertEquals(cajaConservacion.stock, carrito.cantidadTotal())
    }

    @Test
    fun carrito_soloConCursos_noRequiereEnvio() {
        val carrito = CarritoViewModel()
        carrito.agregar(curso)
        assertFalse(carrito.requiereEnvio())

        carrito.agregar(taza)
        assertTrue(carrito.requiereEnvio())
    }

    @Test
    fun carrito_disminuirHastaCero_eliminaElProducto() {
        val carrito = CarritoViewModel()
        carrito.agregar(taza)

        carrito.disminuir(taza.id)

        assertTrue(carrito.items.value.isEmpty())
    }

    @Test
    fun carrito_calculaPesoTotal() {
        val carrito = CarritoViewModel()
        carrito.agregar(taza)
        carrito.agregar(taza)
        carrito.agregar(curso)   // el curso pesa 0

        assertEquals(0.8, carrito.pesoTotalKg(), 0.0001)
    }

    @Test
    fun formatoDePrecio_usaPuntoDeMiles() {
        assertEquals("$8.990", formatearPrecio(8_990))
        assertEquals("$119.990", formatearPrecio(119_990))
        assertEquals("$1.250.000", formatearPrecio(1_250_000))
        assertEquals("$0", formatearPrecio(0))
    }
}
