package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.CatalogoApi
import com.example.appmuseo.data.remote.NominatimApi
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.data.remote.dto.DireccionOsmDto
import com.example.appmuseo.data.remote.dto.LugarOsmDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.repository.CatalogoRepository
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.viewmodel.MapaViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MapaCatalogoRemotoSpec : FunSpec({

    val reloj = TestCoroutineScheduler()

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher(reloj)) }
    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
        CatalogoRepository.restaurarDatosLocales()
        MuseoRepository.restaurarDatosLocales()
    }

    test("el catálogo del servidor reemplaza la copia local con precios y stock al día") {
        val api = mockk<CatalogoApi>()
        coEvery { api.productos() } returns Response.success(
            listOf(ProductoDto(1, "Taza Museo de los Museos", "Taza", 9_990, "MERCH", 3, 0.4, "MDM-001", null, null, true))
        )
        val repositorio = CatalogoRepository(api)

        repositorio.sincronizar().shouldBeInstanceOf<ResultadoApi.Exito<*>>()

        repositorio.obtenerCatalogo().size shouldBe 1
        repositorio.buscarPorCodigo("mdm-001")?.precio shouldBe 9_990
        CatalogoRepository.desdeServidor shouldBe true
        repositorio.catalogoFlow.value.size shouldBe 1
    }

    test("si el catálogo falla se mantienen los 12 productos guardados") {
        val api = mockk<CatalogoApi>()
        coEvery { api.productos() } returns Response.error(500, "{}".toResponseBody("application/json".toMediaType()))
        val repositorio = CatalogoRepository(api)

        repositorio.sincronizar().shouldBeInstanceOf<ResultadoApi.ErrorServidor>()
        repositorio.obtenerCatalogo().size shouldBe 12

        CatalogoRepository(api = null).sincronizar().shouldBeInstanceOf<ResultadoApi.SinConexion>()
        MuseoRepository(api = null).sincronizar().shouldBeInstanceOf<ResultadoApi.SinConexion>()
    }

    test("el mapa muestra un aviso cuando el servidor no responde") {
        val api = mockk<CatalogoApi>()
        coEvery { api.museos() } throws IOException("sin red")
        val viewModel = MapaViewModel(MuseoRepository(api))

        viewModel.sincronizar()
        reloj.advanceUntilIdle()

        viewModel.estado.value.museos.size shouldBe 14
        viewModel.estado.value.red.aviso!! shouldContain "Sin conexión"
        viewModel.estado.value.red.cargando shouldBe false
    }

    test("la ficha del museo muestra la dirección que entrega OpenStreetMap") {
        val osm = mockk<NominatimApi>()
        coEvery { osm.direccion(any(), any(), any(), any(), any()) } returns Response.success(
            LugarOsmDto("Lima", null, null, DireccionOsmDto(null, null, "Cercado de Lima", "Lima", null, null, "Perú"))
        )
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.osm } returns osm
        val viewModel = MapaViewModel(MuseoRepository())
        val andino = viewModel.buscarMuseo(5)!!

        viewModel.cargarDireccion(andino)
        reloj.advanceUntilIdle()

        viewModel.direccionOsm.value shouldBe "Cercado de Lima, Lima, Perú"
    }

    test("si OpenStreetMap no responde la ficha lo explica") {
        val osm = mockk<NominatimApi>()
        coEvery { osm.direccion(any(), any(), any(), any(), any()) } throws IOException("sin red")
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.osm } returns osm
        val viewModel = MapaViewModel(MuseoRepository())

        viewModel.cargarDireccion(viewModel.buscarMuseo(1)!!)
        reloj.advanceUntilIdle()

        viewModel.direccionOsm.value shouldBe "Sin conexión a internet"
    }
})
