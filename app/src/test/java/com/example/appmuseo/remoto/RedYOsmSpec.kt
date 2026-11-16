package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.NominatimApi
import com.example.appmuseo.data.remote.Red
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.aModelo
import com.example.appmuseo.data.remote.aPerfil
import com.example.appmuseo.data.remote.dto.DireccionOsmDto
import com.example.appmuseo.data.remote.dto.LugarOsmDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.model.Rol
import com.example.appmuseo.repository.OsmRepository
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

class RedYOsmSpec : StringSpec({

    "una respuesta 204 sin cuerpo cuenta como éxito" {
        Red.llamar { Response.success<Unit>(204, null) }.shouldBeInstanceOf<ResultadoApi.Exito<*>>()
    }

    "un error sin JSON muestra un mensaje con el código" {
        val resultado = Red.llamar<String> { Response.error(502, "Bad Gateway".toResponseBody("text/plain".toMediaType())) }

        resultado.shouldBeInstanceOf<ResultadoApi.ErrorServidor>()
        resultado.codigo shouldBe 502
        resultado.mensaje shouldBe "Error 502 del servidor"
    }

    "una respuesta que no se puede leer no cierra la app" {
        Red.llamar<String> { throw IllegalStateException("JSON mal formado") }
            .shouldBeInstanceOf<ResultadoApi.ErrorServidor>()
    }

    "los productos del backend se convierten en productos físicos o cursos" {
        val curso = ProductoDto(10, "Curso", "Online", 89_990, "CURSO", 25, 0.0, null, "Online", 6, true).aModelo()
        val taza = ProductoDto(1, "Taza", "Cerámica", 8_990, "MERCH", 25, 0.4, "MDM-001", null, null, false).aModelo()
        val desconocido = ProductoDto(2, "X", "Y", 1_000, "OTRA", 1, 0.1, null, null, null, false).aModelo()

        curso.shouldBeInstanceOf<Curso>()
        curso.cupos shouldBe 25
        taza.shouldBeInstanceOf<ProductoFisico>()
        taza.codigoBarra shouldBe "MDM-001"
        desconocido.categoria shouldBe Categoria.MERCH
    }

    "el usuario del backend se convierte en el perfil de la app" {
        UsuarioDto(3, "Ana", "ana@museo.cl", "Estudiante", "INVESTIGADOR", 2).aPerfil().rol shouldBe Rol.INVESTIGADOR
        UsuarioDto(3, "Ana", "ana@museo.cl", "Estudiante", "OTRO", 2).aPerfil().rol shouldBe Rol.CLIENTE
    }

    "OpenStreetMap se consulta una sola vez por coordenada" {
        val api = mockk<NominatimApi>()
        coEvery { api.direccion(-12.05, -77.04, any(), any(), any()) } returns Response.success(
            LugarOsmDto("Lima", "-12.05", "-77.04", DireccionOsmDto("Jirón de la Unión", null, null, "Lima", null, null, "Perú"))
        )
        val repositorio = OsmRepository(api)

        val primera = repositorio.direccion(-12.05, -77.04)
        val segunda = repositorio.direccion(-12.05, -77.04)

        primera.shouldBeInstanceOf<ResultadoApi.Exito<String>>()
        primera.datos shouldBe "Jirón de la Unión, Lima, Perú"
        segunda.shouldBeInstanceOf<ResultadoApi.Exito<String>>()
        coVerify(exactly = 1) { api.direccion(any(), any(), any(), any(), any()) }
    }

    "si OpenStreetMap falla se informa sin romper la ficha" {
        val api = mockk<NominatimApi>()
        coEvery { api.direccion(any(), any(), any(), any(), any()) } returns Response.error(429, "{}".toResponseBody("application/json".toMediaType()))
        OsmRepository(api).direccion(1.0, 1.0).shouldBeInstanceOf<ResultadoApi.ErrorServidor>()

        coEvery { api.direccion(any(), any(), any(), any(), any()) } throws IOException("sin red")
        OsmRepository(api).direccion(2.0, 2.0).shouldBeInstanceOf<ResultadoApi.SinConexion>()
    }
})
