package com.example.appmuseo.remoto

import com.example.appmuseo.data.remote.UsuariosApi
import com.example.appmuseo.data.remote.dto.LoginRequestDto
import com.example.appmuseo.data.remote.dto.UsuarioDto
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.model.Rol
import com.example.appmuseo.repository.AuthRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class AuthRepositorySpec : BehaviorSpec({

    val json = "application/json".toMediaType()

    Given("un inicio de sesión contra ms-usuarios") {

        When("el servidor acepta las credenciales") {
            val api = mockk<UsuariosApi>()
            val enviado = slot<LoginRequestDto>()
            coEvery { api.login(capture(enviado)) } returns Response.success(
                UsuarioDto(1, "Administrador Demo", "admin@museo.cl", "Profesional", "ADMINISTRADOR", 3)
            )
            val resultado = AuthRepository(demoraMs = 0, api = api).login(" Admin@Museo.cl ", "museo123", Rol.ADMINISTRADOR)

            Then("entra con el perfil y el rol que devuelve el backend") {
                resultado.shouldBeInstanceOf<ResultadoLogin.Exito>()
                resultado.perfil.rol shouldBe Rol.ADMINISTRADOR
            }
            Then("el correo viaja limpio y con el rol elegido") {
                enviado.captured.correo shouldBe "admin@museo.cl"
                enviado.captured.rol shouldBe "ADMINISTRADOR"
            }
        }

        When("la contraseña es incorrecta (401)") {
            val api = mockk<UsuariosApi>()
            coEvery { api.login(any()) } returns Response.error(
                401, """{"estado":401,"mensaje":"Correo o contraseña incorrectos"}""".toResponseBody(json)
            )
            val resultado = AuthRepository(demoraMs = 0, api = api).login("admin@museo.cl", "mala")

            Then("muestra el mensaje del servidor") {
                resultado.shouldBeInstanceOf<ResultadoLogin.Error>()
                resultado.mensaje shouldBe "Correo o contraseña incorrectos"
            }
        }

        When("no hay conexión con el servidor") {
            val api = mockk<UsuariosApi>()
            coEvery { api.login(any()) } throws IOException("sin red")
            val resultado = AuthRepository(demoraMs = 0, api = api).login("estudiante@museo.cl", "duoc2026")

            Then("usa las cuentas guardadas en el teléfono") {
                resultado.shouldBeInstanceOf<ResultadoLogin.Exito>()
                resultado.perfil.rol shouldBe Rol.CLIENTE
            }
        }
    }

    Given("un registro de cuenta nueva") {

        When("la contraseña no cumple la regla del backend (400)") {
            val api = mockk<UsuariosApi>()
            coEvery { api.registrar(any()) } returns Response.error(
                400,
                """{"estado":400,"mensaje":"Hay datos inválidos","campos":{"clave":"Debe combinar letras y números"}}"""
                    .toResponseBody(json)
            )
            val resultado = AuthRepository(demoraMs = 0, api = api)
                .registrar("Ana Pérez", "ana@museo.cl", "abcdef", "Estudiante")

            Then("se muestra el mensaje del campo con error") {
                resultado.shouldBeInstanceOf<ResultadoRegistro.Error>()
                resultado.mensaje shouldBe "Debe combinar letras y números"
                coVerify(exactly = 1) { api.registrar(any()) }
            }
        }
    }
})
