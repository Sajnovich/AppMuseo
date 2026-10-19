package com.example.appmuseo.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.BotonesCamaraGaleria
import com.example.appmuseo.ui.components.DialogoCerrarSesion
import com.example.appmuseo.ui.components.ImagenInteligente
import com.example.appmuseo.ui.theme.AmbarPendiente
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.Notificaciones
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

/**
 * PERFIL: foto con cámara o galería (Guía 13), preferencia de notificaciones guardada en
 * DataStore con color animado y texto derivado (Guía 12), y cierre de sesión con confirmación.
 */
@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    sesionViewModel: SesionViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val imagenUri by perfilViewModel.imagenUri.collectAsState()
    val notificaciones by perfilViewModel.notificacionesActivas.collectAsState()
    val mostrarMensaje by perfilViewModel.mostrarMensaje.collectAsState()

    val context = LocalContext.current

    // Si niega el permiso, la preferencia igual se activa, pero los avisos se verán solo dentro de la app
    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        perfilViewModel.alternarNotificaciones()
        if (!concedido) {
            Toast.makeText(context, "Sin permiso, los avisos se verán solo dentro de la app", Toast.LENGTH_LONG).show()
        }
    }

    var mostrarDialogoSalir by remember { mutableStateOf(false) }
    if (mostrarDialogoSalir) {
        DialogoCerrarSesion(
            onConfirmar = {
                mostrarDialogoSalir = false
                sesionViewModel.cerrarSesion()
                carritoViewModel.vaciar()
                // popUpTo Home inclusive: se borra todo el historial y "atrás" no vuelve a la app
                mainViewModel.navigateTo(Screen.Login, popUpToRoute = Screen.Home, inclusive = true)
            },
            onCancelar = { mostrarDialogoSalir = false }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mi perfil") },
        bottomBar = { BarraInferior(Screen.Perfil.route, mainViewModel::navegarPestana) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto de perfil (componente reutilizable)
            ImagenInteligente(uri = imagenUri)
            BotonesCamaraGaleria(onImagenObtenida = { uri -> perfilViewModel.actualizarImagen(uri) })

            Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall)
            Text(usuario.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tipo de usuario", style = MaterialTheme.typography.labelMedium)
                    Text(usuario.tipoUsuario.ifBlank { "Público general" }, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)

            // Mientras DataStore no responde (null) mostramos un indicador de carga
            val activas = notificaciones
            if (activas == null) {
                CircularProgressIndicator()
            } else {
                // Color animado según el estado (verde = activadas, ámbar = desactivadas)
                val colorAnimado by animateColorAsState(
                    targetValue = if (activas) VerdeCompletado else AmbarPendiente,
                    animationSpec = tween(durationMillis = 500),
                    label = "colorNotificaciones"
                )
                // derivedStateOf: el texto se recalcula solo cuando cambia "activas"
                val textoBoton by remember(activas) {
                    derivedStateOf { if (activas) "Desactivar notificaciones" else "Activar notificaciones" }
                }

                Button(
                    onClick = {
                        // Al ACTIVARLAS en Android 13+ se pide el permiso; al desactivarlas no hace falta
                        if (!activas && Notificaciones.necesitaPermiso(context)) {
                            lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            perfilViewModel.alternarNotificaciones()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorAnimado, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        if (activas) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(textoBoton)
                }
            }

            AnimatedVisibility(visible = mostrarMensaje) {
                Text(
                    "¡Preferencia guardada en el teléfono!",
                    color = VerdeCompletado,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { mostrarDialogoSalir = true },   // primero se pide confirmación
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}
