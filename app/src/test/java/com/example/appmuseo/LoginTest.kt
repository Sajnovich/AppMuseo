package com.example.appmuseo

import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.viewmodel.LoginViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.delay
import com.example.appmuseo.model.Rol

class LoginTest {

    // demoraMs = 0 para que las pruebas no esperen 1,5
    private val repositorio = AuthRepository(demoraMs = 0)

    @Test
    fun login_conCredencialesCorrectas_devuelveExito() = runBlocking {
        val resultado = repositorio.login("admin@museo.cl", "museo123")

        assertTrue(resultado is ResultadoLogin.Exito)
        assertEquals("Profesional", (resultado as ResultadoLogin.Exito).perfil.tipoUsuario)
    }

    @Test
    fun login_ignoraMayusculasYEspaciosEnElCorreo() = runBlocking {
        val resultado = repositorio.login("  ADMIN@Museo.cl ", "museo123")

        assertTrue(resultado is ResultadoLogin.Exito)
    }

    @Test
    fun login_conClaveIncorrecta_devuelveError() = runBlocking {
        val resultado = repositorio.login("admin@museo.cl", "000000")

        assertTrue(resultado is ResultadoLogin.Error)
    }

    @Test
    fun viewModel_conCamposVacios_noValidaYMuestraErrores() {
        val viewModel = LoginViewModel(repositorio)

        val esValido = viewModel.validarFormulario()

        assertFalse(esValido)
        assertNotNull(viewModel.estado.value.errores.correo)
        assertNotNull(viewModel.estado.value.errores.clave)
    }

    @Test
    fun viewModel_conDatosBienEscritos_esValido() {
        val viewModel = LoginViewModel(repositorio)
        viewModel.onCorreoChange("estudiante@museo.cl")
        viewModel.onClaveChange("duoc2026")

        assertTrue(viewModel.validarFormulario())
    }

    @Test
    fun viewModel_alEscribirSeBorraElErrorDelCampo() {
        val viewModel = LoginViewModel(repositorio)
        viewModel.validarFormulario()
        viewModel.onCorreoChange("admin@museo.cl")

        assertNull(viewModel.estado.value.errores.correo)
    }

    @Test
    fun viewModel_cuentaDeOtroRol_noDejaEntrar() = runBlocking {
        val viewModel = LoginViewModel(AuthRepository(demoraMs = 0))
        viewModel.onRolChange(Rol.INVESTIGADOR)
        viewModel.onCorreoChange("admin@museo.cl")
        viewModel.onClaveChange("museo123")

        viewModel.iniciarSesion()
        delay(100)

        assertTrue(viewModel.estado.value.resultado is ResultadoLogin.Error)
    }

    @Test
    fun login_usuarioDePrueba_traeSuRol() = runBlocking {
        val resultado = AuthRepository(demoraMs = 0).login("investigador@museo.cl", "inves2026")

        assertEquals(Rol.INVESTIGADOR, (resultado as ResultadoLogin.Exito).perfil.rol)
    }
}
