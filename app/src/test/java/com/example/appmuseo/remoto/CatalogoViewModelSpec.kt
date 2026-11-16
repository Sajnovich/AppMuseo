package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.viewmodel.CatalogoViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// kotlinx-coroutines-test: el viewModelScope usa Dispatchers.Main, que en las pruebas se reemplaza
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelSpec : FunSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val taza = ProductoFisico(1, "Taza", "Taza de cerámica", 8_990, Categoria.MERCH, 25, 0.4, "MDM-001")
    val tazaConNuevoPrecio = taza.copy(precio = 9_990, stock = 3)

    test("al sincronizar se muestran los precios y el stock del servidor") {
        val repositorio = mockk<CatalogoRepository>()
        every { repositorio.obtenerCatalogo() } returnsMany listOf(listOf<Producto>(taza), listOf<Producto>(tazaConNuevoPrecio))
        coEvery { repositorio.sincronizar() } returns ResultadoApi.Exito(listOf<Producto>(tazaConNuevoPrecio))

        val viewModel = CatalogoViewModel(repositorio)
        viewModel.sincronizar()

        val estado = viewModel.estado.value
        estado.productos.first().precio shouldBe 9_990
        estado.red.desdeServidor shouldBe true
        estado.red.cargando shouldBe false
        estado.red.aviso shouldBe null
    }

    test("sin conexión avisa y mantiene el catálogo guardado") {
        val repositorio = mockk<CatalogoRepository>()
        every { repositorio.obtenerCatalogo() } returns listOf<Producto>(taza)
        coEvery { repositorio.sincronizar() } returns ResultadoApi.SinConexion()

        val viewModel = CatalogoViewModel(repositorio)
        viewModel.sincronizar()

        val estado = viewModel.estado.value
        estado.productos.size shouldBe 1
        estado.red.desdeServidor shouldBe false
        estado.red.aviso!! shouldContain "Sin conexión"
    }
})
