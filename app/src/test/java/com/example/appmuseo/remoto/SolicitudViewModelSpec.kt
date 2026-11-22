package com.example.appmuseo.remoto

import android.app.Application
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.remote.PedidosApi
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.data.remote.dto.LineaPedidoDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.PedidoRequestDto
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.viewmodel.SolicitudViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

// SolicitudViewModel: la compra completa contra ms-pedidos, con Room y Retrofit simulados por MockK
@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModelSpec : FunSpec({

    val reloj = TestCoroutineScheduler()
    val dao = mockk<SolicitudDao>(relaxed = true)
    val api = mockk<PedidosApi>()
    val json = "application/json".toMediaType()

    val taza = ProductoFisico(1, "Taza Museo de los Museos", "Taza", 8_990, Categoria.MERCH, 25, 0.4, "MDM-001")
    val carrito = listOf(ItemCarrito(taza, 2))

    val pedidoServidor = PedidoDto(
        id = 12, codigo = "#2026-00012", correoUsuario = "estudiante@museo.cl",
        nombreCliente = "Estudiante Demo", correoCliente = "estudiante@museo.cl",
        zonaEnvio = "Región Metropolitana", tipoEnvio = "ESTANDAR", direccion = "Av. Siempre Viva 742",
        subtotal = 17_980, costoEnvio = 3_980, total = 21_960, neto = 18_454, iva = 3_506,
        cantidadProductos = 2, estado = "PENDIENTE", fecha = "2026-11-16T10:30:00",
        lineas = listOf(LineaPedidoDto(1, "Taza Museo de los Museos", 2, 8_990, 17_980))
    )

    fun crearViewModel(): SolicitudViewModel {
        val app = mockk<Application>(relaxed = true)
        val base = mockk<AppDatabase>()
        every { base.solicitudDao() } returns dao
        mockkObject(AppDatabase.Companion)
        every { AppDatabase.obtenerBaseDeDatos(any()) } returns base
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.pedidos } returns api
        // sin notificaciones del sistema en las pruebas
        mockkConstructor(SesionDataStore::class)
        every { anyConstructed<SesionDataStore>().obtenerNotificaciones() } returns flowOf(false)
        every { dao.obtenerPorUsuario(any()) } returns flowOf(emptyList())
        coEvery { api.listar(any()) } returns Response.success(emptyList())
        return SolicitudViewModel(app).apply {
            cargarHistorial("estudiante@museo.cl")
            onNombreChange("Estudiante Demo")
            onCorreoChange("estudiante@museo.cl")
            onDireccionChange("Av. Siempre Viva 742")
            onAceptaTerminosChange(true)
            usarEnvio(CotizadorEnvio.cotizar(CotizadorEnvio.zonas.first(), 0.8, 17_980))
        }
    }

    beforeTest {
        // cada prueba parte con los mocks limpios (sin llamadas de la prueba anterior)
        clearMocks(dao, api)
        Dispatchers.setMain(UnconfinedTestDispatcher(reloj))
    }
    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    test("la compra se crea en el servidor y se guarda con los montos que calculó ms-pedidos") {
        val viewModel = crearViewModel()
        val enviado = slot<PedidoRequestDto>()
        val guardado = slot<SolicitudEntity>()
        coEvery { api.crear(capture(enviado)) } returns Response.success(pedidoServidor)
        coEvery { dao.insertar(capture(guardado)) } returns 1L
        var exito = false

        viewModel.confirmarSolicitud(carrito) { exito = true }
        reloj.advanceUntilIdle()

        exito shouldBe true
        enviado.captured.items.first().cantidad shouldBe 2
        enviado.captured.zona shouldBe "Región Metropolitana"
        enviado.captured.tipoEnvio shouldBe "ESTANDAR"
        guardado.captured.idRemoto shouldBe 12L
        guardado.captured.total shouldBe 21_960
        guardado.captured.tipo shouldBe "COMPRA"
        viewModel.guardando.value shouldBe false
    }

    test("si el servidor rechaza la compra (sin stock) se muestra el motivo y no se guarda nada") {
        val viewModel = crearViewModel()
        coEvery { api.crear(any()) } returns Response.error(
            409, """{"estado":409,"mensaje":"No hay stock suficiente"}""".toResponseBody(json)
        )
        var exito = false

        viewModel.confirmarSolicitud(carrito) { exito = true }
        reloj.advanceUntilIdle()

        exito shouldBe false
        viewModel.formulario.value.errorServidor shouldBe "No hay stock suficiente"
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    test("sin conexión la compra se guarda solo en el teléfono") {
        val viewModel = crearViewModel()
        val guardado = slot<SolicitudEntity>()
        coEvery { api.crear(any()) } throws IOException("sin red")
        coEvery { dao.insertar(capture(guardado)) } returns 2L

        viewModel.confirmarSolicitud(carrito) {}
        reloj.advanceUntilIdle()

        guardado.captured.idRemoto shouldBe null
        guardado.captured.subtotal shouldBe 17_980
    }

    test("un formulario incompleto no llama al servidor") {
        val viewModel = crearViewModel()
        viewModel.onAceptaTerminosChange(false)

        viewModel.confirmarSolicitud(carrito) {}

        viewModel.formulario.value.errores.terminos shouldBe "Debes aceptar los términos y condiciones"
        coVerify(exactly = 0) { api.crear(any()) }
    }

    test("avanzar el estado de un pedido del servidor actualiza también la copia local") {
        val viewModel = crearViewModel()
        coEvery { api.avanzar(12) } returns Response.success(pedidoServidor.copy(estado = "CONFIRMADA"))
        val fila = SolicitudEntity(id = 3, tipo = "COMPRA", detalle = "", nombreCliente = "", correoCliente = "",
            zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, idRemoto = 12)

        viewModel.avanzarEstado(fila) shouldBe EstadoSolicitud.CONFIRMADA
        reloj.advanceUntilIdle()

        coVerify { dao.actualizarEstado(3, EstadoSolicitud.CONFIRMADA) }
    }

    test("si el servidor no deja avanzar el estado, se avisa y no se cambia nada") {
        val viewModel = crearViewModel()
        coEvery { api.avanzar(12) } returns Response.error(
            409, """{"estado":409,"mensaje":"El pedido ya fue entregado"}""".toResponseBody(json)
        )
        val fila = SolicitudEntity(id = 3, tipo = "COMPRA", detalle = "", nombreCliente = "", correoCliente = "",
            zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, idRemoto = 12)

        viewModel.avanzarEstado(fila)

        viewModel.textoNotificacion.value shouldBe "El pedido ya fue entregado"
        coVerify(exactly = 0) { dao.actualizarEstado(any(), any()) }
        reloj.advanceUntilIdle()
    }

    test("anular un pedido lo borra del servidor y del teléfono") {
        val viewModel = crearViewModel()
        coEvery { api.anular(12) } returns Response.success(Unit)
        val fila = SolicitudEntity(id = 3, tipo = "COMPRA", detalle = "", nombreCliente = "", correoCliente = "",
            zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, idRemoto = 12)

        viewModel.eliminar(fila)
        reloj.advanceUntilIdle()

        coVerify { dao.eliminar(fila) }
    }

    test("un pedido ya enviado no se puede anular") {
        val viewModel = crearViewModel()
        coEvery { api.anular(12) } returns Response.error(
            409, """{"estado":409,"mensaje":"El pedido ya fue enviada y no se puede anular"}""".toResponseBody(json)
        )
        val fila = SolicitudEntity(id = 3, tipo = "COMPRA", detalle = "", nombreCliente = "", correoCliente = "",
            zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, idRemoto = 12)

        viewModel.eliminar(fila)

        coVerify(exactly = 0) { dao.eliminar(any()) }
        reloj.advanceUntilIdle()
    }

    test("sin conexión no se borra un pedido que existe en el servidor") {
        val viewModel = crearViewModel()
        coEvery { api.anular(12) } throws IOException("sin red")
        val fila = SolicitudEntity(id = 3, tipo = "COMPRA", detalle = "", nombreCliente = "", correoCliente = "",
            zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, idRemoto = 12)

        viewModel.eliminar(fila)

        viewModel.textoNotificacion.value shouldBe "Sin conexión: no se pudo anular el pedido en el servidor"
        coVerify(exactly = 0) { dao.eliminar(any()) }
        reloj.advanceUntilIdle()
    }

    test("al cargar el historial se sincronizan los pedidos hechos en otro teléfono") {
        val viewModel = crearViewModel()
        coEvery { api.listar("otro@museo.cl") } returns Response.success(listOf(pedidoServidor))
        coEvery { dao.buscarPorIdRemoto(12) } returns null
        coEvery { dao.insertar(any()) } returns 5L

        viewModel.cargarHistorial("otro@museo.cl")
        reloj.advanceUntilIdle()

        coVerify { dao.insertar(match { it.idRemoto == 12L && it.correoUsuario == "otro@museo.cl" }) }
    }
})
