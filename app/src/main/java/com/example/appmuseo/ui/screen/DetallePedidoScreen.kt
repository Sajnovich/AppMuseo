package com.example.appmuseo.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.model.Boleta
import com.example.appmuseo.model.TipoNotificacion
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.utils.formatearFecha
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.utils.textoBoleta
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

private val NEGRO = Color(0xFF111111)
private val GRIS = Color(0xFF666666)

// detalle de una compra: pestaña Seguimiento (estado) y pestaña Boleta
@Composable
fun DetallePedidoScreen(
    pedidoId: Int,
    solicitudViewModel: SolicitudViewModel,
    notificacionesViewModel: NotificacionesViewModel,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel
) {
    // se observa la lista para que el estado se actualice al avanzar
    val solicitudes by solicitudViewModel.solicitudes.collectAsState()
    val pedido = solicitudes.find { it.id == pedidoId }
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Pedido ${codigoSolicitud(pedidoId)}",
                onVolver = { mainViewModel.navigateBack() }
            )
        },
        bottomBar = {
            if (pedido != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Intent implicito: Android ofrece WhatsApp, correo, etc.
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Boleta ${codigoSolicitud(pedido.id)}")
                                    putExtra(Intent.EXTRA_TEXT, textoBoleta(pedido))
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir boleta"))
                            },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir")
                        }
                        Button(
                            onClick = {
                                Boleta.decodificar(pedido.lineas).forEach { linea ->
                                    catalogoViewModel.buscarPorId(linea.productoId)?.let { carritoViewModel.agregarCantidad(it, linea.cantidad) }
                                }
                                mainViewModel.navigateTo(Screen.Carrito)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Volver a comprar")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (pedido == null) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Este pedido ya no está en tu historial")
            }
            return@Scaffold
        }

        if (confirmarEliminar) {
            DialogoConfirmacion(
                titulo = "¿Eliminar el pedido ${codigoSolicitud(pedido.id)}?",
                mensaje = "Se borrará de tu historial. Esta acción no se puede deshacer.",
                icono = Icons.Filled.Delete,
                textoConfirmar = "Eliminar",
                esDestructiva = true,
                onConfirmar = {
                    confirmarEliminar = false
                    solicitudViewModel.eliminar(pedido)
                    mainViewModel.navigateBack()
                },
                onCancelar = { confirmarEliminar = false }
            )
        }

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = pestana,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { posiciones ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(posiciones[pestana]), color = DoradoBoton)
                }
            ) {
                Tab(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    text = { Text("Seguimiento") },
                    icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null) }
                )
                Tab(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    text = { Text("Boleta") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (pestana == 0) {
                    Seguimiento(
                        pedido = pedido,
                        onAvanzar = {
                            val nuevo = solicitudViewModel.avanzarEstado(pedido)
                            if (nuevo != null) {
                                notificacionesViewModel.agregar(
                                    "Tu pedido está ${EstadoSolicitud.etiqueta(nuevo).lowercase()}",
                                    "El pedido ${codigoSolicitud(pedido.id)} cambió de estado.",
                                    TipoNotificacion.SOLICITUD
                                )
                            }
                        },
                        onEliminar = { confirmarEliminar = true }
                    )
                } else {
                    BoletaPapel(pedido)
                }
            }
        }
    }
}

// ---------- Pestaña Seguimiento ----------

private fun descripcionEstado(estado: String): String = when (estado) {
    EstadoSolicitud.PENDIENTE -> "Recibimos tu solicitud"
    EstadoSolicitud.CONFIRMADA -> "El pago y el stock fueron confirmados"
    EstadoSolicitud.ENVIADA -> "Tu pedido va en camino"
    else -> "Pedido entregado. ¡Gracias por tu compra!"
}

@Composable
private fun Seguimiento(pedido: SolicitudEntity, onAvanzar: () -> Unit, onEliminar: () -> Unit) {
    val indiceActual = EstadoSolicitud.ORDEN.indexOf(pedido.estado)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado del pedido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(formatearFecha(pedido.fecha), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // linea de tiempo vertical: punto + linea + texto
            EstadoSolicitud.ORDEN.forEachIndexed { i, estado ->
                val alcanzado = i <= indiceActual
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (alcanzado) DoradoBoton else MaterialTheme.colorScheme.outlineVariant)
                        )
                        if (i < EstadoSolicitud.ORDEN.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(34.dp)
                                    .background(if (i < indiceActual) DoradoBoton else MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            EstadoSolicitud.etiqueta(estado),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (i == indiceActual) FontWeight.Bold else FontWeight.Normal,
                            color = if (alcanzado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (i == indiceActual) {
                            Text(descripcionEstado(estado), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Despacho", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (pedido.zonaEnvio.isBlank()) {
                Text("No aplica: solo cursos (acceso por correo)", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(pedido.zonaEnvio, style = MaterialTheme.typography.bodyMedium)
                Text(pedido.direccion, style = MaterialTheme.typography.bodyMedium)
            }
            Text("${pedido.cantidadProductos} artículos · ${formatearPrecio(pedido.total)}", style = MaterialTheme.typography.bodyMedium, color = AzulNoche)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (pedido.estado != EstadoSolicitud.ENTREGADA) {
            OutlinedButton(onClick = onAvanzar) {
                Icon(Icons.Filled.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Avanzar estado (simulado)")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onEliminar) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar pedido", tint = MaterialTheme.colorScheme.error)
        }
    }
}

// ---------- Pestaña Boleta (blanco y negro, como una boleta impresa) ----------

@Composable
private fun BoletaPapel(pedido: SolicitudEntity) {
    val lineas = Boleta.decodificar(pedido.lineas)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFDDDDDD))
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Text(
            "MUSEO DE LOS MUSEOS",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = NEGRO,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        TextoBoleta("Boleta electrónica (simulada)", centrado = true)
        TextoBoleta("N° ${codigoSolicitud(pedido.id).removePrefix("#")}", centrado = true)
        TextoBoleta(formatearFecha(pedido.fecha), centrado = true)

        LineaCortada()
        FilaBoleta("Cliente", pedido.nombreCliente)
        FilaBoleta("Correo", pedido.correoCliente)
        if (pedido.zonaEnvio.isNotBlank()) {
            FilaBoleta("Despacho", pedido.zonaEnvio)
            FilaBoleta("Dirección", pedido.direccion)
        }

        LineaCortada()
        Row {
            TextoBoleta("Cant", modifier = Modifier.width(40.dp), color = GRIS)
            TextoBoleta("Producto", modifier = Modifier.weight(1f), color = GRIS)
            TextoBoleta("Total", color = GRIS)
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (lineas.isEmpty()) {
            // compras hechas antes de guardar las lineas
            TextoBoleta(pedido.detalle)
        }
        lineas.forEach { linea ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                TextoBoleta("${linea.cantidad}", modifier = Modifier.width(40.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TextoBoleta(linea.nombre)
                    TextoBoleta("${formatearPrecio(linea.precioUnitario)} c/u", color = GRIS)
                }
                TextoBoleta(formatearPrecio(linea.total))
            }
        }

        LineaCortada()
        FilaMontoBoleta("Subtotal", pedido.subtotal)
        FilaMontoBoleta("Envío", pedido.costoEnvio)
        FilaMontoBoleta("Neto", Boleta.neto(pedido.total))
        FilaMontoBoleta("IVA 19%", Boleta.iva(pedido.total))

        LineaCortada()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NEGRO, modifier = Modifier.weight(1f))
            Text(formatearPrecio(pedido.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NEGRO)
        }
        Spacer(modifier = Modifier.height(14.dp))
        TextoBoleta("Gracias por su compra", centrado = true)
        TextoBoleta("Documento sin validez tributaria", centrado = true, color = GRIS)
    }
}

@Composable
private fun TextoBoleta(
    texto: String,
    modifier: Modifier = Modifier,
    centrado: Boolean = false,
    color: Color = NEGRO
) {
    Text(
        texto,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = color,
        textAlign = if (centrado) TextAlign.Center else TextAlign.Start,
        modifier = if (centrado) modifier.fillMaxWidth() else modifier
    )
}

@Composable
private fun FilaBoleta(etiqueta: String, valor: String) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        TextoBoleta(etiqueta, modifier = Modifier.width(80.dp), color = GRIS)
        TextoBoleta(valor, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FilaMontoBoleta(etiqueta: String, monto: Int) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        TextoBoleta(etiqueta, modifier = Modifier.weight(1f))
        TextoBoleta(formatearPrecio(monto))
    }
}

// linea punteada que separa las partes de la boleta
@Composable
private fun LineaCortada() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFF888888),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }
    )
}
