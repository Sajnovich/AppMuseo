package com.example.appmuseo

import com.example.appmuseo.data.local.UsuarioDao
import com.example.appmuseo.data.local.UsuarioEntity
import com.example.appmuseo.model.RegistroUiState
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.model.TipoUsuario
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.utils.Seguridad
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.viewmodel.RegistroViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeUsuarioDao : UsuarioDao {
    val usuarios = mutableMapOf<String, UsuarioEntity>()

    override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? = usuarios[correo]

    override suspend fun insertar(usuario: UsuarioEntity) {
        usuarios[usuario.correo] = usuario
    }
}

class RegistroTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepository(demoraMs = 0, usuarioDao = dao)

    @Test
    fun registrar_cuentaNueva_yLuegoIniciarSesion() = runBlocking {
        val registro = repositorio.registrar("Ana Pérez", "Ana@Correo.cl ", "clave123", "Estudiante")
        val login = repositorio.login("ana@correo.cl", "clave123")

        assertTrue(registro is ResultadoRegistro.Exito)
        assertTrue(login is ResultadoLogin.Exito)
        assertEquals("Ana Pérez", (login as ResultadoLogin.Exito).perfil.nombre)
    }

    @Test
    fun registrar_noGuardaLaClaveEnTextoPlano() = runBlocking {
        repositorio.registrar("Ana Pérez", "ana@correo.cl", "clave123", "Estudiante")

        val guardado = dao.usuarios["ana@correo.cl"]!!
        assertNotEquals("clave123", guardado.claveHash)
        assertEquals(Seguridad.hashSha256("clave123"), guardado.claveHash)
    }

    @Test
    fun registrar_correoRepetido_devuelveError() = runBlocking {
        repositorio.registrar("Ana Pérez", "ana@correo.cl", "clave123", "Estudiante")

        val repetido = repositorio.registrar("Otra Ana", "ana@correo.cl", "otra456", "Profesional")
        val correoDePrueba = repositorio.registrar("Admin", "admin@museo.cl", "clave123", "Profesional")

        assertTrue(repetido is ResultadoRegistro.Error)
        assertTrue(correoDePrueba is ResultadoRegistro.Error)
    }

    @Test
    fun login_conClaveEquivocada_deUnaCuentaRegistrada_falla() = runBlocking {
        repositorio.registrar("Ana Pérez", "ana@correo.cl", "clave123", "Estudiante")

        assertTrue(repositorio.login("ana@correo.cl", "clave999") is ResultadoLogin.Error)
    }

    @Test
    fun hash_esSiempreIgualParaElMismoTexto_yMide64() {
        val hash = Seguridad.hashSha256("museo123")

        assertEquals(hash, Seguridad.hashSha256("museo123"))
        assertEquals(64, hash.length)
        assertNotEquals(hash, Seguridad.hashSha256("museo124"))
    }

    @Test
    fun claveSegura_exigeLetrasYNumeros() {
        assertNotNull(Validaciones.validarClaveSegura("abcdefg"))
        assertNotNull(Validaciones.validarClaveSegura("1234567"))
        assertNull(Validaciones.validarClaveSegura("museo2026"))
    }

    @Test
    fun confirmacion_distinta_esError() {
        assertNotNull(Validaciones.validarConfirmacion("clave123", "clave124"))
        assertNull(Validaciones.validarConfirmacion("clave123", "clave123"))
    }

    @Test
    fun registro_vacio_marcaTodosLosCampos() {
        val errores = Validaciones.validarRegistro(RegistroUiState())

        assertNotNull(errores.nombre)
        assertNotNull(errores.correo)
        assertNotNull(errores.clave)
        assertNotNull(errores.confirmarClave)
        assertNotNull(errores.tipoUsuario)
        assertNotNull(errores.terminos)
    }

    @Test
    fun viewModel_formularioCompleto_esValido_yAlCorregirSeBorraElError() {
        val viewModel = RegistroViewModel(repositorio)
        viewModel.validarFormulario()   // genera errores
        viewModel.onNombreChange("Ana Pérez")
        assertNull(viewModel.estado.value.errores.nombre)   // se borro solo el error del nombre

        viewModel.onCorreoChange("ana@correo.cl")
        viewModel.onClaveChange("clave123")
        viewModel.onConfirmarClaveChange("clave123")
        viewModel.onTipoUsuarioChange(TipoUsuario.ESTUDIANTE)
        viewModel.onAceptaTerminosChange(true)

        assertTrue(viewModel.validarFormulario())
        assertFalse(viewModel.estado.value.errores.hayErrores())
    }

    @Test
    fun registro_vacio_listaTodosLosCamposObligatorios() {
        val errores = Validaciones.validarRegistro(RegistroUiState())

        val faltantes = errores.camposConError()

        assertTrue(faltantes.containsAll(listOf("Nombre", "Correo", "Contraseña", "Términos y condiciones")))
    }
}
