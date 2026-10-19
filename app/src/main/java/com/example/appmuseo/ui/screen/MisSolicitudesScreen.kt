package com.example.appmuseo.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.components.NotificacionSimulada
import com.example.appmuseo.ui.theme.AmbarPendiente
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.formatearFecha
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

@Composable
fun MisSolicitudesScreen(solicitudViewModel: SolicitudViewModel, mainViewModel: MainViewModel) {
    val solicitudes by solicitudViewModel.solicitudes.collectAsState()
    val mostrarNotificacion by solicitudViewModel.mostrarNotificacion.collectAsState()
    val textoNotificacion by solicitudViewModel.textoNotificacion.collectAsState()

    // filtro local de la pantalla
    var filtroEstado by rememberSaveable { mutableStateOf<String?>(null) }
    val visibles = if (filtroEstado == null) solicitudes else solicitudes.filter { it.estado == filtroEstado }

    var solicitudAEliminar by remember { mutableStateOf<SolicitudEntity?>(null) }
    solicitudAEliminar?.let { solicitud ->
        DialogoConfirmacion(
            titulo = "¿Eliminar la solicitud #${solicitud.id}?",
            mensaje = "Se borrará del historial de este teléfono. Esta acción no se puede deshacer.",
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
        topBar = { BarraSuperior(titulo = "Mis solicitudes", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NotificacionSimulada(visible = mostrarNotificacion, mensaje = textoNotificacion)

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filtroEstado == null, onClick = { filtroEstado = null }, label = { Text("Todas") })
                FilterChip(
                    selected = filtroEstado == EstadoSolicitud.PENDIENTE,
                    onClick = { filtroEstado = EstadoSolicitud.PENDIENTE },
                    label = { Text("Pendientes") }
                )
                FilterChip(
                    selected = filtroEstado == EstadoSolicitud.COMPLETADA,
                    onClick = { filtroEstado = EstadoSolicitud.COMPLETADA },
                    label = { Text("Completadas") }
                )
            }

            if (visibles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aún no hay solicitudes aquí", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { mainViewModel.navegarPestana(Screen.Catalogo) }) { Text("Explorar catálogo") }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibles, key = { it.id }) { solicitud ->
                        TarjetaSolicitud(
                            solicitud = solicitud,
                            modifier = Modifier.animateItem(),
                            onCompletar = { solicitudViewModel.marcarCompletada(solicitud) },
                            onEliminar = { solicitudAEliminar = solicitud }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSolicitud(
    solicitud: SolicitudEntity,
    onCompletar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completada = solicitud.estado == EstadoSolicitud.COMPLETADA
    val colorEstado by animateColorAsState(
        targetValue = if (completada) VerdeCompletado else AmbarPendiente,
        animationSpec = tween(durationMillis = 500),
        label = "colorEstado"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Solicitud #${solicitud.id} · ${solicitud.tipo}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (completada) "Completada" else "Pendiente") },
                    leadingIcon = {
                        Icon(
                            if (completada) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = Color.White,
                        leadingIconContentColor = Color.White,
                        containerColor = colorEstado
                    )
                )
            }
            Text(solicitud.detalle, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Registrada el ${formatearFecha(solicitud.fecha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (solicitud.zonaEnvio.isNotBlank()) {
                Text("Envío a: ${solicitud.zonaEnvio}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Total: ${formatearPrecio(solicitud.total)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!completada) {
                    OutlinedButton(onClick = onCompletar) { Text("Marcar como completada") }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar solicitud ${solicitud.id}")
                }
            }
        }
    }
}
