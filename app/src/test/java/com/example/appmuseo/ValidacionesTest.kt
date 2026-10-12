package com.example.appmuseo

import com.example.appmuseo.model.LoginUiState
import com.example.appmuseo.model.SolicitudUiState
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import com.example.appmuseo.utils.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PRUEBAS UNITARIAS de las reglas de validación.
 * Estructura de cada prueba (patrón AAA):
 *   Arrange = preparar datos · Act = ejecutar · Assert = comprobar el resultado
 */
class ValidacionesTest {

    @Test
    fun correo_conFormatoCorrecto_esValido() {
        assertTrue(Validaciones.esCorreoValido("admin@museo.cl"))
        assertTrue(Validaciones.esCorreoValido("nombre.apellido@duocuc.cl"))
    }

    @Test
    fun correo_sinArrobaOSinDominio_esInvalido() {
        assertFalse(Validaciones.esCorreoValido("adminmuseo.cl"))
        assertFalse(Validaciones.esCorreoValido("admin@"))
        assertFalse(Validaciones.esCorreoValido("admin@museo"))
    }

    @Test
    fun correo_vacio_entregaMensajeDeObligatorio() {
        assertEquals("El correo es obligatorio", Validaciones.validarCorreo("   "))
    }

    @Test
    fun clave_corta_entregaError() {
        assertNotNull(Validaciones.validarClave("123"))
        assertNull(Validaciones.validarClave("museo123"))
    }

    @Test
    fun login_conCamposVacios_tieneDosErrores() {
        val errores = Validaciones.validarLogin(LoginUiState(correo = "", clave = ""))

        assertNotNull(errores.correo)
        assertNotNull(errores.clave)
        assertTrue(errores.hayErrores())
    }

    @Test
    fun solicitud_soloCursos_noExigeDireccionNiZona() {
        val estado = SolicitudUiState(nombre = "Ana Pérez", correo = "ana@correo.cl", aceptaTerminos = true)

        val errores = Validaciones.validarSolicitud(estado, requiereEnvio = false)

        assertFalse(errores.hayErrores())
    }

    @Test
    fun solicitud_conEnvio_exigeDireccionYZona() {
        val estado = SolicitudUiState(nombre = "Ana Pérez", correo = "ana@correo.cl", aceptaTerminos = true)

        val errores = Validaciones.validarSolicitud(estado, requiereEnvio = true)

        assertNotNull(errores.direccion)
        assertNotNull(errores.zona)
    }

    @Test
    fun solicitud_completaConEnvio_esValida() {
        val zona = Zona("Región Metropolitana", TipoDestino.NACIONAL, 2_990, 990, 1, 2)
        val estado = SolicitudUiState(
            nombre = "Ana Pérez", correo = "ana@correo.cl", direccion = "Av. Siempre Viva 123",
            zona = zona, aceptaTerminos = true
        )

        assertFalse(Validaciones.validarSolicitud(estado, requiereEnvio = true).hayErrores())
    }

    @Test
    fun solicitud_sinAceptarTerminos_esInvalida() {
        val estado = SolicitudUiState(nombre = "Ana Pérez", correo = "ana@correo.cl", aceptaTerminos = false)

        val errores = Validaciones.validarSolicitud(estado, requiereEnvio = false)

        assertNotNull(errores.terminos)
    }
}
