package com.example.appmuseo

import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.model.EditarPerfilUiState
import com.example.appmuseo.model.ResultadoActualizacion
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.Rol
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilHistorialTest {

    private val dao = FakeUsuarioDao()
    private val repositorio = AuthRepository(demoraMs = 0, usuarioDao = dao)

    @Test
    fun cuentaDePrueba_seCopiaARoomConSuRol() = runBlocking {
        val resultado = repositorio.login("investigador@museo.cl", "inves2026")

        assertEquals(Rol.INVESTIGADOR, (resultado as ResultadoLogin.Exito).perfil.rol)
        assertNotNull(dao.usuarios["investigador@museo.cl"])
    }

    @Test
    fun cambiarCorreo_conElNuevoEntraYConElAnteriorNo() = runBlocking {
        val r = repositorio.actualizarPerfil("estudiante@museo.cl", "Estudiante Nueva", "nuevo@museo.cl", "duoc2026")

        assertTrue(r is ResultadoActualizacion.Exito)
        assertTrue(repositorio.login("nuevo@museo.cl", "duoc2026") is ResultadoLogin.Exito)
        assertTrue(repositorio.login("estudiante@museo.cl", "duoc2026") is ResultadoLogin.Error)
    }

    @Test
    fun cambiarClave_laNuevaFuncionaYLaViejaNo() = runBlocking {
        repositorio.actualizarPerfil("admin@museo.cl", "Administrador Demo", "admin@museo.cl", "museo123", "nueva2026")

        assertTrue(repositorio.login("admin@museo.cl", "nueva2026") is ResultadoLogin.Exito)
        assertTrue(repositorio.login("admin@museo.cl", "museo123") is ResultadoLogin.Error)
    }

    @Test
    fun claveActualIncorrecta_noGuardaNada() = runBlocking {
        val r = repositorio.actualizarPerfil("admin@museo.cl", "Otro", "otro@museo.cl", "equivocada")

        assertEquals("claveActual", (r as ResultadoActualizacion.Error).campo)
        assertNull(dao.usuarios["otro@museo.cl"])
    }

    @Test
    fun correoDeOtraCuenta_daErrorEnElCorreo() = runBlocking {
        repositorio.login("admin@museo.cl", "museo123")   // crea las cuentas de prueba
        val r = repositorio.actualizarPerfil("admin@museo.cl", "Admin", "estudiante@museo.cl", "museo123")

        assertEquals("correo", (r as ResultadoActualizacion.Error).campo)
    }

    @Test
    fun validarEdicion_sinClaveActual_marcaError() {
        val errores = Validaciones.validarEdicionPerfil(EditarPerfilUiState(nombre = "Ana Pérez", correo = "ana@correo.cl"))
        assertNotNull(errores.claveActual)
        assertNull(errores.claveNueva)
    }

    @Test
    fun validarEdicion_clavesNuevasDistintas_marcaError() {
        val estado = EditarPerfilUiState(
            nombre = "Ana Pérez", correo = "ana@correo.cl", claveActual = "x",
            cambiarClave = true, claveNueva = "nueva2026", repetirClave = "otra2026"
        )
        assertNotNull(Validaciones.validarEdicionPerfil(estado).repetirClave)
    }

    @Test
    fun estados_avanzanEnOrdenYTerminanEnEntregada() {
        assertEquals(EstadoSolicitud.CONFIRMADA, EstadoSolicitud.siguiente(EstadoSolicitud.PENDIENTE))
        assertEquals(EstadoSolicitud.ENTREGADA, EstadoSolicitud.siguiente(EstadoSolicitud.ENVIADA))
        assertNull(EstadoSolicitud.siguiente(EstadoSolicitud.ENTREGADA))
    }

    @Test
    fun codigoSolicitud_tieneCincoDigitos() {
        assertEquals("#2026-00007", codigoSolicitud(7))
        assertFalse(codigoSolicitud(123).contains(" "))
    }
}
