package com.example.appmuseo.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Notificacion
import com.example.appmuseo.model.TipoNotificacion
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel

private fun iconoDeTipo(tipo: TipoNotificacion): ImageVector = when (tipo) {
    TipoNotificacion.SOLICITUD -> Icons.Filled.Receipt
    TipoNotificacion.CURSO -> Icons.Filled.School
    TipoNotificacion.NOVEDAD -> Icons.Filled.Campaign
    TipoNotificacion.MUSEO -> Icons.Filled.AccountBalance
}

@Composable
fun NotificacionesScreen(viewModel: NotificacionesViewModel, mainViewModel: MainViewModel) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val noLeidas = notificaciones.count { !it.leida }
    // filtro por tipo (null = todas)
    var filtro by rememberSaveable { mutableStateOf<TipoNotificacion?>(null) }
    val visibles = notificaciones.filter { filtro == null || it.tipo == filtro }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Notificaciones",
                acciones = {
                    if (noLeidas > 0) {
                        IconButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Icon(Icons.Filled.DoneAll, contentDescription = "Marcar todas como leídas")
                        }
                    }
                }
            )
        },
        bottomBar = { BarraInferior(Screen.Notificaciones.route, mainViewModel::navegarPestana, noLeidas) }
    ) { innerPadding ->
        if (notificaciones.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No tienes notificaciones", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = filtro == null, onClick = { filtro = null }, label = { Text("Todas") }) }
                        items(TipoNotificacion.entries) { tipo ->
                            FilterChip(
                                selected = filtro == tipo,
                                onClick = { filtro = if (filtro == tipo) null else tipo },
                                label = { Text(tipo.etiqueta) },
                                leadingIcon = { Icon(iconoDeTipo(tipo), contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
                item {
                    Text(
                        if (noLeidas > 0) "Tienes $noLeidas sin leer" else "Estás al día",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(visibles, key = { it.id }) { notificacion ->
                    FilaNotificacion(
                        notificacion = notificacion,
                        modifier = Modifier.animateItem(),
                        onAbrir = { viewModel.marcarLeida(notificacion.id) },
                        onEliminar = { viewModel.eliminar(notificacion.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaNotificacion(
    notificacion: Notificacion,
    modifier: Modifier = Modifier,
    onAbrir: () -> Unit,
    onEliminar: () -> Unit
) {
    // las no leidas se ven con fondo mas marcado
    val fondo = if (notificacion.leida) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer
    Card(
        onClick = onAbrir,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoDeTipo(notificacion.tipo), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    notificacion.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                )
                Text(notificacion.mensaje, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${notificacion.tipo.etiqueta} · ${notificacion.hace}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Eliminar notificación", modifier = Modifier.size(18.dp))
            }
        }
    }
}
