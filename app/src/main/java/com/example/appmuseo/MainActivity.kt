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
import com.example.appmuseo.utils.ImagenesCatalogo
import com.example.appmuseo.utils.Notificaciones
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notificaciones.crearCanal(this)
        // lee las imágenes de productos y museos guardadas en el teléfono
        ImagenesCatalogo.iniciar(this)
        // osmdroid exige identificar la app ante los servidores
        Configuration.getInstance().userAgentValue = "AppMuseoDuoc/1.0 (proyecto estudiantil)"
        setContent {
            MuseoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
