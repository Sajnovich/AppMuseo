package com.example.appmuseo.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.TipoNotificacion
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.MiniaturaProducto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.Notificaciones
import com.example.appmuseo.utils.formatearFecha
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

// Paso 3: el detalle completo antes de confirmar
@Composable
fun ResumenCompraScreen(
    solicitudViewModel: SolicitudViewModel,
    carritoViewModel: CarritoViewModel,
    notificacionesViewModel: NotificacionesViewModel,
    mainViewModel: MainViewModel
) {
    val formulario by solicitudViewModel.formulario.collectAsState()
    val items by carritoViewModel.items.collectAsState()
    val envio by carritoViewModel.envio.collectAsState()
    val guardando by solicitudViewModel.guardando.collectAsState()
    val resumen = remember(items, envio) { carritoViewModel.resumen() }
    val context = LocalContext.current
    val hoy = remember { formatearFecha(System.currentTimeMillis()) }

    fun confirmar() {
        val total = resumen.total
        solicitudViewModel.confirmarSolicitud(items) {
            notificacionesViewModel.agregar(
                "Compra registrada",
                "Tu solicitud por ${formatearPrecio(total)} quedó pendiente de confirmación.",
                TipoNotificacion.SOLICITUD
            )
            carritoViewModel.vaciar()
            mainViewModel.navigateTo(Screen.MisSolicitudes, popUpToRoute = Screen.Home)
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> confirmar() }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Resumen de compra", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PasosCompra(pasoActual = 3)

            // tipo "boleta"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AzulNoche)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = DoradoBotonClaro)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Museo de los Museos", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(hoy, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                    Text(
                        "${resumen.cantidadProductos} art.",
                        style = MaterialTheme.typography.labelLarge,
                        color = DoradoBotonClaro
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MiniaturaProducto(item.producto, tamano = 48.dp)
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(item.producto.nombre, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (item.producto is Curso) "Inscripción" else "${item.cantidad} × ${formatearPrecio(item.producto.precio)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(formatearPrecio(item.subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    LineaPunteada()

                    FilaMonto("Subtotal", formatearPrecio(resumen.subtotal))
                    if (resumen.requiereEnvio) {
                        val cotizacion = resumen.cotizacion
                        FilaMonto(
                            "Envío ${cotizacion?.tipoEnvio?.etiqueta?.lowercase() ?: ""}",
                            if (cotizacion?.envioGratis == true) "Gratis" else formatearPrecio(resumen.costoEnvio)
                        )
                        cotizacion?.let {
                            Text(
                                "${it.zona.nombre} · ${it.plazo} · ${it.pesoFacturable} kg facturable",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        FilaMonto("Envío", "No aplica (solo cursos)")
                    }

                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(formatearPrecio(resumen.total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DoradoBoton)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Datos de entrega", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(formulario.nombre, style = MaterialTheme.typography.bodyMedium)
                    Text(formulario.correo, style = MaterialTheme.typography.bodyMedium)
                    if (resumen.requiereEnvio) Text(formulario.direccion, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    if (Notificaciones.necesitaPermiso(context)) {
                        lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        confirmar()
                    }
                },
                enabled = resumen.listoParaPagar && !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar compra · ${formatearPrecio(resumen.total)}")
                }
            }
            Text(
                "Pago simulado: la solicitud queda guardada en el dispositivo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// 1 Carrito - 2 Datos - 3 Resumen
@Composable
fun PasosCompra(pasoActual: Int) {
    val pasos = listOf("Carrito", "Datos", "Resumen")
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        pasos.forEachIndexed { i, nombre ->
            val numero = i + 1
            val hecho = numero < pasoActual
            val actual = numero == pasoActual
            val color by animateColorAsState(
                when {
                    hecho -> VerdeCompletado
                    actual -> DoradoBoton
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                label = "paso$numero"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (hecho) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    else Text("$numero", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    nombre,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (actual) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (i < pasos.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (numero < pasoActual) VerdeCompletado else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

@Composable
private fun LineaPunteada() {
    val color = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                )
            }
    )
}

@Composable
private fun FilaMonto(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
