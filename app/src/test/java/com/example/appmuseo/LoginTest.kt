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

/** Pruebas del login con credenciales en variable (sin backend). */
class LoginTest {

    // demoraMs = 0 para que las pruebas no esperen 1,5 segundos
    private val repositorio = AuthRepository(demoraMs = 0)

    @Test
    fun login_conCredencialesCorrectas_devuelveExito() = runBlocking {
        // runBlocking permite llamar a una función suspend dentro de la prueba (Guía 5)
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
        viewModel.validarFormulario()                  // genera errores
        viewModel.onCorreoChange("admin@museo.cl")     // el usuario corrige

        assertNull(viewModel.estado.value.errores.correo)
    }
}
