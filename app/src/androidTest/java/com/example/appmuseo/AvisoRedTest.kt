package com.example.appmuseo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appmuseo.model.EstadoRed
import com.example.appmuseo.ui.components.AvisoRed
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de UI con Compose: dibuja el componente en el emulador y revisa lo que ve el usuario
@RunWith(AndroidJUnit4::class)
class AvisoRedTest {

    @get:Rule
    val reglaCompose = createComposeRule()

    @Test
    fun sinConexion_muestraElAvisoYElBotonReintentar() {
        var reintentos = 0
        reglaCompose.setContent {
            AvisoRed(
                red = EstadoRed(aviso = "Sin conexión con el servidor: mostrando datos guardados"),
                onReintentar = { reintentos++ }
            )
        }

        reglaCompose.onNodeWithText("Sin conexión con el servidor: mostrando datos guardados").assertIsDisplayed()
        reglaCompose.onNodeWithText("Reintentar").performClick()

        assertEquals(1, reintentos)
    }

    @Test
    fun conDatosDelServidor_noMuestraNingunAviso() {
        reglaCompose.setContent {
            AvisoRed(red = EstadoRed(desdeServidor = true), onReintentar = {})
        }

        reglaCompose.onNodeWithText("Reintentar").assertDoesNotExist()
    }
}
