package com.example.appmuseo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.ui.components.AvisoRolJwt
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.ChipsOpciones
import com.example.appmuseo.ui.components.MensajeGestion
import com.example.appmuseo.ui.theme.AmbarPendiente
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.GestionViewModel
import com.example.appmuseo.viewmodel.MainViewModel

private val ESTADOS = listOf("PENDIENTE", "CONFIRMADA", "ENVIADA", "ENTREGADA")

private fun etiquetaEstado(estado: String?) = when (estado) {
    null -> "Todas"
    else -> estado.lowercase().replaceFirstChar { it.uppercase() }
}

private fun siguienteEstado(estado: String): String? = ESTADOS.getOrNull(ESTADOS.indexOf(estado) + 1)

private fun colorEstado(estado: String) = when (estado) {
    "PENDIENTE" -> AmbarPendiente
    "CONFIRMADA" -> DoradoBoton
    "ENVIADA" -> AzulNoche
    else -> VerdeCompletado
}

// Pantalla 13 del caso (rol Administrador): revisa las solicitudes de todos los clientes
// y actualiza su estado: Pendiente -> Confirmada -> Enviada -> Entregada
@Composable
fun GestionPedidosScreen(gestionViewModel: GestionViewModel, mainViewModel: MainViewModel) {
    val estado by gestionViewModel.estado.collectAsState()

    LaunchedEffect(Unit) { gestionViewModel.cargarPedidos() }

    Scaffold(
        topBar = {
            BarraSuperior("Solicitudes recibidas", onVolver = { mainViewModel.navigateBack() }) {
                IconButton(onClick = { gestionViewModel.cargarPedidos() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar solicitudes")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (estado.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AvisoRolJwt("Administrador · GET /api/pedidos y PATCH /api/pedidos/{id}/avanzar")
                ChipsOpciones(listOf<String?>(null) + ESTADOS, estado.filtroEstado, ::etiquetaEstado) { gestionViewModel.filtrarPedidos(it) }
                MensajeGestion(estado.mensaje) { gestionViewModel.limpiarMensaje() }
                Text(
                    "${estado.pedidosFiltrados.size} solicitudes · ${estado.pendientes} pendientes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.pedidosFiltrados, key = { it.id }) { pedido ->
                    TarjetaPedido(pedido, enProceso = estado.guardando) { gestionViewModel.avanzarPedido(pedido.id) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedido(pedido: PedidoDto, enProceso: Boolean, onAvanzar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${pedido.codigo}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    etiquetaEstado(pedido.estado),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(colorEstado(pedido.estado), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Text("${pedido.nombreCliente} · ${pedido.correoUsuario}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                pedido.lineas.joinToString { "${it.cantidad}× ${it.nombre}" }.ifBlank { "${pedido.cantidadProductos} artículos" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(formatearPrecio(pedido.total), style = MaterialTheme.typography.titleSmall, color = AzulNoche, modifier = Modifier.weight(1f))
                val siguiente = siguienteEstado(pedido.estado)
                if (siguiente != null) {
                    OutlinedButton(onClick = onAvanzar, enabled = !enProceso) {
                        Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pasar a ${etiquetaEstado(siguiente).lowercase()}")
                    }
                } else {
                    Text("Entregado ✓", color = VerdeCompletado, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
