package com.example.appmuseo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.appmuseo.navigation.AppNavigation
import com.example.appmuseo.ui.theme.MuseoTheme
import com.example.appmuseo.utils.Notificaciones
import org.osmdroid.config.Configuration

/**
 * Punto de entrada de la app. Solo aplica el tema y arranca la navegación;
 * cada pantalla tiene su propio Scaffold, que se encarga de los márgenes del sistema.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // la app dibuja detrás de la barra de estado (como en la Guía 11)
        Notificaciones.crearCanal(this)
        // osmdroid exige identificar la app ante los servidores de OpenStreetMap (antes de crear el mapa)
        Configuration.getInstance().userAgentValue = packageName   // canal de notificaciones (obligatorio desde Android 8)
        setContent {
            MuseoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
