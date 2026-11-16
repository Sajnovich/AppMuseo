package com.example.appmuseo.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.NotificacionSimulada
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.utils.formatearFecha
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

private fun colorDeEstado(estado: String): Color = when (estado) {
    EstadoSolicitud.PENDIENTE -> Color(0xFFB26A00)
    EstadoSolicitud.CONFIRMADA -> Color(0xFF6A3D7C)
    EstadoSolicitud.ENVIADA -> Color(0xFF1F5C9A)
    else -> Color(0xFF2E7D32)
}

@Composable
fun MisSolicitudesScreen(
    solicitudViewModel: SolicitudViewModel,
    notificacionesViewModel: NotificacionesViewModel,
    mainViewModel: MainViewModel
) {
    val solicitudes by solicitudViewModel.solicitudes.collectAsState()
    val mostrarNotificacion by solicitudViewModel.mostrarNotificacion.collectAsState()
    val textoNotificacion by solicitudViewModel.textoNotificacion.collectAsState()

    // al abrir se actualiza el estado de los pedidos desde ms-pedidos
    LaunchedEffect(Unit) { solicitudViewModel.sincronizarHistorial() }

    // filtro local de la pantalla (null = todas)
    var filtroEstado by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = if (filtroEstado == null) solicitudes else solicitudes.filter { it.estado == filtroEstado }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis compras", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NotificacionSimulada(visible = mostrarNotificacion, mensaje = textoNotificacion)

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(selected = filtroEstado == null, onClick = { filtroEstado = null }, label = { Text("Todas (${solicitudes.size})") })
                }
                items(EstadoSolicitud.ORDEN) { estado ->
                    val cantidad = solicitudes.count { it.estado == estado }
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick = { filtroEstado = estado },
                        label = { Text("${EstadoSolicitud.etiqueta(estado)}s ($cantidad)") }
                    )
                }
            }

            if (visibles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (solicitudes.isEmpty()) "Aún no tienes compras" else "No hay compras en este estado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { mainViewModel.navegarPestana(Screen.Catalogo) }) { Text("Explorar catálogo") }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Has gastado ${formatearPrecio(solicitudes.sumOf { it.total })} en ${solicitudes.size} compras",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(visibles, key = { it.id }) { solicitud ->
                        TarjetaSolicitud(
                            solicitud = solicitud,
                            modifier = Modifier.animateItem(),
                            onAbrir = { mainViewModel.navigateTo(Screen.DetallePedido(solicitud.id)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaSolicitud(
    solicitud: SolicitudEntity,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorEstado by animateColorAsState(colorDeEstado(solicitud.estado), label = "colorEstado")

    Card(
        onClick = onAbrir,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(codigoSolicitud(solicitud.id), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(formatearFecha(solicitud.fecha), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    EstadoSolicitud.etiqueta(solicitud.estado),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colorEstado)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${solicitud.tipo.lowercase().replaceFirstChar { it.uppercase() }} · ${solicitud.cantidadProductos} ${if (solicitud.cantidadProductos == 1) "artículo" else "artículos"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(formatearPrecio(solicitud.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulNoche)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver seguimiento y boleta")
            }
        }
    }
}
