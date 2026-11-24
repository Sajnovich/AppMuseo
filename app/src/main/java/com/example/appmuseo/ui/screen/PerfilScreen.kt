package com.example.appmuseo.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.example.appmuseo.data.remote.TokenSesion
import com.example.appmuseo.model.Rol
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BotonesCamaraGaleria
import com.example.appmuseo.ui.components.DialogoCerrarSesion
import com.example.appmuseo.ui.components.FotoPerfil
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.ui.theme.AzulProfundo
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.utils.Notificaciones
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    sesionViewModel: SesionViewModel,
    carritoViewModel: CarritoViewModel,
    solicitudViewModel: SolicitudViewModel,
    favoritosViewModel: FavoritosViewModel,
    mainViewModel: MainViewModel,
    noLeidas: Int = 0
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val imagenUri by perfilViewModel.imagenUri.collectAsState()
    val notificaciones by perfilViewModel.notificacionesActivas.collectAsState()
    val mostrarMensaje by perfilViewModel.mostrarMensaje.collectAsState()
    val solicitudes by solicitudViewModel.solicitudes.collectAsState()
    val museosFav by favoritosViewModel.museos.collectAsState()
    val productosFav by favoritosViewModel.productos.collectAsState()
    val context = LocalContext.current

    var cambiarFoto by remember { mutableStateOf(false) }

    // si niega el permiso, la preferencia igual se activa (los avisos se ven dentro de la app)
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
                mainViewModel.navigateTo(Screen.Bienvenida, popUpToRoute = Screen.Home, inclusive = true)
            },
            onCancelar = { mostrarDialogoSalir = false }
        )
    }

    Scaffold(
        bottomBar = { BarraInferior(Screen.Perfil.route, mainViewModel::navegarPestana, noLeidas) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // encabezado azul con la foto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(AzulNoche, AzulProfundo)))
                    .statusBarsPadding()
                    .padding(top = 20.dp, bottom = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Mi perfil", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.clickable { cambiarFoto = !cambiarFoto }) {
                    FotoPerfil(
                        fotoUri = imagenUri,
                        avatar = usuario.avatar,
                        tamano = 112.dp,
                        modifier = Modifier.border(3.dp, DoradoBotonClaro, CircleShape)
                    )
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Cambiar foto de perfil",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DoradoBoton)
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(usuario.correo, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    usuario.rol.etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulNoche,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DoradoBotonClaro)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .offset(y = (-28).dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // cifras rapidas del usuario
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(vertical = 14.dp)) {
                        Cifra("${solicitudes.size}", "Compras", Modifier.weight(1f))
                        Cifra("${museosFav.size + productosFav.size}", "Favoritos", Modifier.weight(1f))
                        Cifra(formatearPrecio(solicitudes.sumOf { it.total }), "Total", Modifier.weight(1f))
                    }
                }

                AnimatedVisibility(visible = cambiarFoto) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cambiar foto de perfil", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            BotonesCamaraGaleria(onImagenObtenida = { uri ->
                                perfilViewModel.guardarFoto(uri, usuario.correo)
                                cambiarFoto = false
                                Toast.makeText(context, "Foto guardada", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }

                // sesión segura: muestra que la app trabaja con el token JWT que entregó ms-usuarios
                SeccionMenu {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = if (TokenSesion.hayToken()) VerdeCompletado else MaterialTheme.colorScheme.error)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(
                                if (TokenSesion.hayToken()) "Sesión protegida con JWT" else "Sin token (modo sin conexión)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Rol ${usuario.rol.etiqueta}" + (TokenSesion.horaExpiracion()?.let { " · vence a las $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TokenSesion.abreviado()?.let {
                                Text("Bearer $it", style = MaterialTheme.typography.labelSmall, color = AzulNoche, maxLines = 1)
                            }
                        }
                    }
                }

                // funciones propias del rol
                if (usuario.rol != Rol.CLIENTE) {
                    SeccionMenu {
                        if (usuario.rol == Rol.ADMINISTRADOR) {
                            OpcionMenu(Icons.Filled.Inventory2, "Gestionar catálogo", "Alta, modificación y eliminación") {
                                mainViewModel.navigateTo(Screen.GestionCatalogo)
                            }
                            HorizontalDivider()
                            OpcionMenu(Icons.Filled.AssignmentTurnedIn, "Solicitudes recibidas", "Revisar y cambiar estados") {
                                mainViewModel.navigateTo(Screen.GestionPedidos)
                            }
                            HorizontalDivider()
                        }
                        OpcionMenu(Icons.Filled.AccountBalance, "Fichas de museos", "Registrar y corregir fichas museológicas") {
                            mainViewModel.navigateTo(Screen.GestionMuseos)
                        }
                    }
                }

                SeccionMenu {
                    OpcionMenu(Icons.Filled.ManageAccounts, "Mis datos personales", "Nombre, correo y contraseña") {
                        perfilViewModel.iniciarEdicion(usuario)
                        mainViewModel.navigateTo(Screen.EditarPerfil)
                    }
                    HorizontalDivider()
                    OpcionMenu(Icons.Filled.Receipt, "Mis compras", "${solicitudes.size} solicitudes en tu historial") {
                        mainViewModel.navigateTo(Screen.MisSolicitudes)
                    }
                    HorizontalDivider()
                    OpcionMenu(Icons.Filled.Favorite, "Mis favoritos", "Museos y recursos guardados") {
                        mainViewModel.navigateTo(Screen.Favoritos)
                    }
                }

                SeccionMenu {
                    val activas = notificaciones ?: true
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = AzulNoche)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text("Notificaciones", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (activas) "Te avisamos cuando cambie tu pedido" else "Desactivadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = activas,
                            onCheckedChange = {
                                if (!activas && Notificaciones.necesitaPermiso(context)) {
                                    lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    perfilViewModel.alternarNotificaciones()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = DoradoBoton)
                        )
                    }
                }
                AnimatedVisibility(visible = mostrarMensaje) {
                    Text(
                        "¡Preferencia guardada en el teléfono!",
                        color = DoradoBoton,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                TextButton(
                    onClick = { mostrarDialogoSalir = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun Cifra(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulNoche, maxLines = 1)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SeccionMenu(contenido: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { contenido() }
    }
}

@Composable
private fun OpcionMenu(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = AzulNoche)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}
