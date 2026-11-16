package com.example.appmuseo.remoto

import com.example.appmuseo.data.local.SolicitudDao
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.remote.PedidosApi
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.ItemPedidoDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.PedidoRequestDto
import com.example.appmuseo.repository.SolicitudRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class SolicitudRepositorySpec : StringSpec({

    val json = "application/json".toMediaType()

    fun pedido(id: Long, estado: String) = PedidoDto(
        id = id, codigo = "#2026-0000$id", correoUsuario = "estudiante@museo.cl",
        nombreCliente = "Estudiante Demo", correoCliente = "estudiante@museo.cl",
        zonaEnvio = null, tipoEnvio = null, direccion = null,
        subtotal = 89_990, costoEnvio = 0, total = 89_990, neto = 75_622, iva = 14_368,
        cantidadProductos = 1, estado = estado, fecha = null, lineas = emptyList()
    )

    fun filaGuardada(id: Int, idRemoto: Long, estado: String) = SolicitudEntity(
        id = id, tipo = "INSCRIPCIÓN", detalle = "", nombreCliente = "", correoCliente = "",
        zonaEnvio = "", direccion = "", subtotal = 0, costoEnvio = 0, total = 0, estado = estado, idRemoto = idRemoto
    )

    "sincronizar agrega los pedidos nuevos y actualiza el estado de los que ya estaban" {
        val dao = mockk<SolicitudDao>(relaxed = true)
        val api = mockk<PedidosApi>()
        coEvery { api.listar("estudiante@museo.cl") } returns Response.success(
            listOf(pedido(1, "ENVIADA"), pedido(2, "PENDIENTE"), pedido(3, "CONFIRMADA"))
        )
        coEvery { dao.buscarPorIdRemoto(1) } returns filaGuardada(10, 1, "PENDIENTE")   // cambió de estado
        coEvery { dao.buscarPorIdRemoto(2) } returns filaGuardada(11, 2, "PENDIENTE")   // sin cambios
        coEvery { dao.buscarPorIdRemoto(3) } returns null                               // nuevo
        coEvery { dao.insertar(any()) } returns 12L

        val resultado = SolicitudRepository(dao, api).sincronizar("estudiante@museo.cl")

        resultado.shouldBeInstanceOf<ResultadoApi.Exito<Int>>()
        resultado.datos shouldBe 3
        coVerify(exactly = 1) { dao.actualizarEstado(10, "ENVIADA") }
        coVerify(exactly = 1) { dao.insertar(match { it.idRemoto == 3L && it.tipo == "INSCRIPCIÓN" }) }
        coVerify(exactly = 0) { dao.actualizarEstado(11, any()) }
    }

    "si el servidor falla al sincronizar no se toca Room" {
        val dao = mockk<SolicitudDao>(relaxed = true)
        val api = mockk<PedidosApi>()
        coEvery { api.listar(any()) } returns Response.error(500, "{}".toResponseBody(json))

        SolicitudRepository(dao, api).sincronizar("x@museo.cl").shouldBeInstanceOf<ResultadoApi.ErrorServidor>()
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    "sin conexión sincronizar avisa y no rompe la app" {
        val api = mockk<PedidosApi>()
        coEvery { api.listar(any()) } throws IOException("sin red")

        SolicitudRepository(mockk(relaxed = true), api).sincronizar("x@museo.cl")
            .shouldBeInstanceOf<ResultadoApi.SinConexion>()
    }

    "crear, avanzar y anular llaman a los endpoints de ms-pedidos" {
        val api = mockk<PedidosApi>()
        val solicitud = PedidoRequestDto("a@museo.cl", "Ana Pérez", "a@museo.cl", null, null, null, listOf(ItemPedidoDto(10, 1)))
        coEvery { api.crear(solicitud) } returns Response.success(pedido(7, "PENDIENTE"))
        coEvery { api.avanzar(7) } returns Response.success(pedido(7, "CONFIRMADA"))
        coEvery { api.anular(7) } returns Response.success(Unit)
        val repositorio = SolicitudRepository(mockk(relaxed = true), api)

        repositorio.crearEnServidor(solicitud).shouldBeInstanceOf<ResultadoApi.Exito<PedidoDto>>()
        val avanzado = repositorio.avanzarEnServidor(7)
        avanzado.shouldBeInstanceOf<ResultadoApi.Exito<PedidoDto>>()
        avanzado.datos.estado shouldBe "CONFIRMADA"
        repositorio.anularEnServidor(7).shouldBeInstanceOf<ResultadoApi.Exito<Unit>>()
    }

    "sin servidor configurado responde sin conexión" {
        val repositorio = SolicitudRepository(mockk(relaxed = true), api = null)

        repositorio.crearEnServidor(mockk()).shouldBeInstanceOf<ResultadoApi.SinConexion>()
        repositorio.avanzarEnServidor(1).shouldBeInstanceOf<ResultadoApi.SinConexion>()
        repositorio.anularEnServidor(1).shouldBeInstanceOf<ResultadoApi.SinConexion>()
        repositorio.sincronizar("x").shouldBeInstanceOf<ResultadoApi.SinConexion>()
    }

    "la copia local delega en Room" {
        val dao = mockk<SolicitudDao>(relaxed = true)
        val repositorio = SolicitudRepository(dao)
        val fila = filaGuardada(1, 1, "PENDIENTE")

        repositorio.actualizarEstado(1, "ENVIADA")
        repositorio.cambiarDueno("a@museo.cl", "b@museo.cl")
        repositorio.eliminar(fila)

        coVerify { dao.actualizarEstado(1, "ENVIADA") }
        coVerify { dao.cambiarDueno("a@museo.cl", "b@museo.cl") }
        coVerify { dao.eliminar(fila) }
    }

    "el tipo de envío express se muestra con su etiqueta" {
        SolicitudRepository.etiquetaEnvio("EXPRESS") shouldBe "Express"
        SolicitudRepository.etiquetaEnvio(null) shouldBe "Estándar"
    }
})
