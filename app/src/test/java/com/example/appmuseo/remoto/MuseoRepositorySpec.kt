package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.CatalogoApi
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.repository.MuseoRepository
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

// Kotest + MockK: la API de Retrofit se reemplaza por un "doble" (mock), así no se necesita el servidor
class MuseoRepositorySpec : StringSpec({

    val museoDelServidor = MuseoDto(
        id = 5, nombre = "Museo Virtual Andino", pais = "Perú", ciudad = "Lima", tipo = "Arqueología",
        descripcion = "Modelos 3D", detalle = null, latitud = -12.05, longitud = -77.04,
        anioLevantamiento = 2025, calificacion = 4.9, resenas = 210
    )

    // el repositorio guarda una copia compartida: se limpia para que una prueba no afecte a otra
    afterTest { MuseoRepository.restaurarDatosLocales() }

    "sincronizar reemplaza la copia local por los museos del servidor" {
        val api = mockk<CatalogoApi>()
        coEvery { api.museos() } returns Response.success(listOf(museoDelServidor))
        val repositorio = MuseoRepository(api)

        val resultado = repositorio.sincronizar()

        resultado.shouldBeInstanceOf<ResultadoApi.Exito<*>>()
        repositorio.obtenerMuseos().size shouldBe 1
        repositorio.buscarPorId(5)?.detalle shouldBe ""
        MuseoRepository.desdeServidor shouldBe true
        coVerify(exactly = 1) { api.museos() }
    }

    "si el servidor responde con error se informa el mensaje y se mantienen los 14 museos" {
        val api = mockk<CatalogoApi>()
        val cuerpo = """{"estado":500,"mensaje":"Base de datos caída"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.museos() } returns Response.error(500, cuerpo)
        val repositorio = MuseoRepository(api)

        val resultado = repositorio.sincronizar()

        resultado.shouldBeInstanceOf<ResultadoApi.ErrorServidor>()
        resultado.mensaje shouldBe "Base de datos caída"
        repositorio.obtenerMuseos().size shouldBe 14
    }

    "sin conexión la app sigue funcionando con la copia local" {
        val api = mockk<CatalogoApi>()
        coEvery { api.museos() } throws IOException("sin red")
        val repositorio = MuseoRepository(api)

        repositorio.sincronizar().shouldBeInstanceOf<ResultadoApi.SinConexion>()
        repositorio.buscarPorCodigo("MUSEO-5")?.nombre shouldBe "Museo Virtual Andino"
        MuseoRepository.desdeServidor shouldBe false
    }
})
