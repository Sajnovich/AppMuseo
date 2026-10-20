package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.model.TipoNotificacion
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.components.NotificacionSimulada
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
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

    // filtro local de la pantalla (null = todas)
    var filtroEstado by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = if (filtroEstado == null) solicitudes else solicitudes.filter { it.estado == filtroEstado }

    var solicitudAEliminar by remember { mutableStateOf<SolicitudEntity?>(null) }
    solicitudAEliminar?.let { solicitud ->
        DialogoConfirmacion(
            titulo = "¿Eliminar la solicitud ${codigoSolicitud(solicitud.id)}?",
            mensaje = "Se borrará de tu historial. Esta acción no se puede deshacer.",
            icono = Icons.Filled.Delete,
            textoConfirmar = "Eliminar",
            esDestructiva = true,
            onConfirmar = {
                solicitudViewModel.eliminar(solicitud)
                solicitudAEliminar = null
            },
            onCancelar = { solicitudAEliminar = null }
        )
    }

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
                            onAvanzar = {
                                val nuevo = solicitudViewModel.avanzarEstado(solicitud)
                                if (nuevo != null) {
                                    notificacionesViewModel.agregar(
                                        "Tu pedido está ${EstadoSolicitud.etiqueta(nuevo).lowercase()}",
                                        "El pedido ${codigoSolicitud(solicitud.id)} cambió de estado.",
                                        TipoNotificacion.SOLICITUD
                                    )
                                }
                            },
                            onEliminar = { solicitudAEliminar = solicitud }
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
    onAvanzar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var abierta by rememberSaveable(solicitud.id) { mutableStateOf(false) }
    val giro by animateFloatAsState(if (abierta) 180f else 0f, label = "flecha")
    val colorEstado by animateColorAsState(colorDeEstado(solicitud.estado), label = "colorEstado")

    Card(
        onClick = { abierta = !abierta },
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
                Icon(Icons.Filled.ExpandMore, contentDescription = if (abierta) "Ocultar detalle" else "Ver detalle", modifier = Modifier.rotate(giro))
            }

            AnimatedVisibility(visible = abierta) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider()
                    LineaDeTiempo(solicitud.estado)
                    Text(solicitud.detalle, style = MaterialTheme.typography.bodySmall)
                    if (solicitud.zonaEnvio.isNotBlank()) {
                        Text("Despacho: ${solicitud.zonaEnvio}", style = MaterialTheme.typography.bodySmall)
                        Text("Dirección: ${solicitud.direccion}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Subtotal ${formatearPrecio(solicitud.subtotal)} + envío ${formatearPrecio(solicitud.costoEnvio)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (solicitud.estado != EstadoSolicitud.ENTREGADA) {
                            OutlinedButton(onClick = onAvanzar) {
                                Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Avanzar estado (simulado)")
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar solicitud", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// 4 puntos unidos por una linea: se pintan los estados ya alcanzados
@Composable
private fun LineaDeTiempo(estadoActual: String) {
    val indiceActual = EstadoSolicitud.ORDEN.indexOf(estadoActual)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        EstadoSolicitud.ORDEN.forEachIndexed { i, estado ->
            val alcanzado = i <= indiceActual
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(if (i == 0) Color.Transparent else if (alcanzado) DoradoBoton else MaterialTheme.colorScheme.outlineVariant)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (alcanzado) DoradoBoton else MaterialTheme.colorScheme.outlineVariant)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (i == EstadoSolicitud.ORDEN.lastIndex) Color.Transparent
                                else if (i < indiceActual) DoradoBoton else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    EstadoSolicitud.etiqueta(estado),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (i == indiceActual) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
