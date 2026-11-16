package com.example.appmuseo.remoto

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.remote.UsuariosApi
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.ResultadoActualizacion
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.repository.AuthRepository
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

class AuthRepositoryRemotoSpec : StringSpec({

    val json = "application/json".toMediaType()
    val cuenta = UsuarioDto(4, "Ana Pérez", "ana@museo.cl", "Estudiante", "CLIENTE", 1)

    "registrar crea la cuenta en el servidor y guarda una copia en Room" {
        val api = mockk<UsuariosApi>()
        val dao = mockk<UsuarioDao>(relaxed = true)
        coEvery { api.registrar(any()) } returns Response.success(cuenta)
        coEvery { dao.buscarPorCorreo("ana@museo.cl") } returns null

        val resultado = AuthRepository(demoraMs = 0, usuarioDao = dao, api = api)
            .registrar("Ana Pérez", "Ana@Museo.cl", "ana2026", "Estudiante", 1)

        resultado.shouldBeInstanceOf<ResultadoRegistro.Exito>()
        resultado.correo shouldBe "ana@museo.cl"
        coVerify { dao.insertar(match { it.correo == "ana@museo.cl" && it.rol == "CLIENTE" }) }
    }

    "registrar un correo repetido muestra el mensaje del servidor" {
        val api = mockk<UsuariosApi>()
        coEvery { api.registrar(any()) } returns Response.error(
            409, """{"estado":409,"mensaje":"Ya existe una cuenta con ese correo"}""".toResponseBody(json)
        )

        val resultado = AuthRepository(demoraMs = 0, api = api).registrar("Ana", "ana@museo.cl", "ana2026", "Estudiante")

        resultado.shouldBeInstanceOf<ResultadoRegistro.Error>()
        resultado.mensaje shouldBe "Ya existe una cuenta con ese correo"
    }

    "sin conexión no se puede registrar" {
        val api = mockk<UsuariosApi>()
        coEvery { api.registrar(any()) } throws IOException("sin red")

        AuthRepository(demoraMs = 0, api = api).registrar("Ana", "ana@museo.cl", "ana2026", "Estudiante")
            .shouldBeInstanceOf<ResultadoRegistro.Error>()
    }

    "editar el perfil con un correo que ya usa otra persona marca el campo correo" {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo("ana@museo.cl") } returns Response.success(cuenta)
        coEvery { api.actualizar(4, any()) } returns Response.error(
            409, """{"estado":409,"mensaje":"Ese correo ya está en uso"}""".toResponseBody(json)
        )

        val resultado = AuthRepository(demoraMs = 0, api = api)
            .actualizarPerfil("ana@museo.cl", "Ana", "admin@museo.cl", "ana2026")

        resultado.shouldBeInstanceOf<ResultadoActualizacion.Error>()
        resultado.campo shouldBe "correo"
    }

    "editar el perfil de una cuenta que no existe en el servidor informa el error" {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo(any()) } returns Response.error(
            404, """{"estado":404,"mensaje":"No existe una cuenta con el correo x@museo.cl"}""".toResponseBody(json)
        )

        val resultado = AuthRepository(demoraMs = 0, api = api).actualizarPerfil("x@museo.cl", "X", "x@museo.cl", "clave1")

        resultado.shouldBeInstanceOf<ResultadoActualizacion.Error>()
        resultado.mensaje shouldBe "No existe una cuenta con el correo x@museo.cl"
    }

    "editar el perfil sin conexión no guarda nada" {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo(any()) } throws IOException("sin red")

        AuthRepository(demoraMs = 0, api = api).actualizarPerfil("ana@museo.cl", "Ana", "ana@museo.cl", "ana2026")
            .shouldBeInstanceOf<ResultadoActualizacion.Error>()
        coVerify(exactly = 0) { api.actualizar(any(), any()) }
    }

    "una validación del servidor muestra el mensaje del campo" {
        val api = mockk<UsuariosApi>()
        coEvery { api.buscarPorCorreo(any()) } returns Response.success(cuenta)
        coEvery { api.actualizar(any(), any()) } returns Response.error(
            400, """{"estado":400,"mensaje":"Datos inválidos","campos":{"nuevaClave":"Mínimo 6 caracteres, con letras y números"}}""".toResponseBody(json)
        )

        val resultado = AuthRepository(demoraMs = 0, api = api)
            .actualizarPerfil("ana@museo.cl", "Ana", "ana@museo.cl", "ana2026", nuevaClave = "abc")

        resultado.shouldBeInstanceOf<ResultadoActualizacion.Error>()
        resultado.mensaje shouldBe "Mínimo 6 caracteres, con letras y números"
    }
})
