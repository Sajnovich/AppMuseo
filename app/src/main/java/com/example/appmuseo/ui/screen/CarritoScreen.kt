package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Cotizacion
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.OpcionesEnvio
import com.example.appmuseo.model.ResumenCompra
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.TipoEnvio
import com.example.appmuseo.model.Zona
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.components.MiniaturaProducto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonContenedor
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel

@Composable
fun CarritoScreen(carritoViewModel: CarritoViewModel, mainViewModel: MainViewModel) {
    // al observar items y envio, la pantalla se vuelve a dibujar y se recalcula el resumen
    val items by carritoViewModel.items.collectAsState()
    val envio by carritoViewModel.envio.collectAsState()
    val resumen = remember(items, envio) { carritoViewModel.resumen() }
    var confirmarVaciar by remember { mutableStateOf(false) }

    if (confirmarVaciar) {
        DialogoConfirmacion(
            titulo = "¿Vaciar el carrito?",
            mensaje = "Se quitarán todos los productos y cursos.",
            icono = Icons.Filled.DeleteOutline,
            textoConfirmar = "Vaciar",
            onConfirmar = {
                carritoViewModel.vaciar()
                confirmarVaciar = false
            },
            onCancelar = { confirmarVaciar = false },
            esDestructiva = true
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mi carrito",
                onVolver = { mainViewModel.navigateBack() },
                acciones = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { confirmarVaciar = true }) { Text("Vaciar") }
                    }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                PanelTotales(resumen = resumen, onContinuar = { mainViewModel.navigateTo(Screen.Solicitud) })
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { mainViewModel.navegarPestana(Screen.Catalogo) }) { Text("Ver catálogo") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { PasosCompra(pasoActual = 1) }
                item {
                    Text(
                        "${resumen.cantidadProductos} ${if (resumen.cantidadProductos == 1) "artículo" else "artículos"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(items, key = { it.producto.id }) { item ->
                    FilaCarrito(
                        item = item,
                        modifier = Modifier.animateItem(),
                        onSumar = { carritoViewModel.agregar(item.producto) },
                        onRestar = { carritoViewModel.disminuir(item.producto.id) },
                        onEliminar = { carritoViewModel.eliminar(item.producto.id) }
                    )
                }
                if (resumen.requiereEnvio) {
                    item {
                        SeccionEnvio(
                            opciones = envio,
                            resumen = resumen,
                            onTipoDestino = carritoViewModel::onTipoDestinoChange,
                            onZona = carritoViewModel::onZonaChange,
                            onTipoEnvio = carritoViewModel::onTipoEnvioChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaCarrito(
    item: ItemCarrito,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniaturaProducto(item.producto, tamano = 68.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(item.producto.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "${formatearPrecio(item.producto.precio)} c/u",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (item.producto is Curso) {
                    Text("Inscripción · 1 cupo", style = MaterialTheme.typography.labelMedium, color = DoradoBoton)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRestar, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.Remove, contentDescription = "Quitar una unidad", modifier = Modifier.size(18.dp))
                        }
                        AnimatedContent(targetState = item.cantidad, label = "cantidad") { cantidad ->
                            Text("$cantidad", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        IconButton(onClick = onSumar, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = "Agregar una unidad", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar ${item.producto.nombre}", tint = MaterialTheme.colorScheme.error)
                }
                Text(formatearPrecio(item.subtotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionEnvio(
    opciones: OpcionesEnvio,
    resumen: ResumenCompra,
    onTipoDestino: (TipoDestino) -> Unit,
    onZona: (Zona) -> Unit,
    onTipoEnvio: (TipoEnvio) -> Unit
) {
    var desplegado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = AzulNoche)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calcular envío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoDestino.entries.forEach { tipo ->
                    FilterChip(
                        selected = opciones.tipoDestino == tipo,
                        onClick = { onTipoDestino(tipo) },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }

            // lista desplegable con las zonas del tipo elegido
            ExposedDropdownMenuBox(expanded = desplegado, onExpandedChange = { desplegado = it }) {
                OutlinedTextField(
                    value = opciones.zona?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Destino") },
                    placeholder = { Text("Elige una zona") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = desplegado, onDismissRequest = { desplegado = false }) {
                    CotizadorEnvio.zonasPorTipo(opciones.tipoDestino).forEach { zona ->
                        DropdownMenuItem(
                            text = { Text(zona.nombre) },
                            onClick = {
                                onZona(zona)
                                desplegado = false
                            }
                        )
                    }
                }
            }

            val zona = opciones.zona
            if (zona != null && resumen.errorEnvio == null) {
                Text("Tipo de envío", style = MaterialTheme.typography.labelLarge)
                TipoEnvio.entries.forEach { tipo ->
                    // se cotiza cada opcion para mostrar su precio
                    val cotizacion = remember(zona, tipo, resumen.pesoKg, resumen.subtotal) {
                        CotizadorEnvio.cotizar(zona, resumen.pesoKg, resumen.subtotal, tipo)
                    }
                    OpcionEnvio(
                        cotizacion = cotizacion,
                        icono = if (tipo == TipoEnvio.EXPRESS) Icons.Filled.Bolt else Icons.Filled.LocalShipping,
                        seleccionada = opciones.tipoEnvio == tipo,
                        onClick = { onTipoEnvio(tipo) }
                    )
                }
            }
            resumen.errorEnvio?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            // barra de avance hacia el envio gratis (solo chile)
            if (opciones.tipoDestino == TipoDestino.NACIONAL) {
                val avance by animateFloatAsState(
                    (resumen.subtotal.toFloat() / CotizadorEnvio.MONTO_ENVIO_GRATIS).coerceAtMost(1f),
                    label = "avanceGratis"
                )
                HorizontalDivider()
                Text(
                    if (resumen.faltaParaEnvioGratis == 0) "¡Tu compra tiene envío estándar gratis en Chile!"
                    else "Te faltan ${formatearPrecio(resumen.faltaParaEnvioGratis)} para envío gratis",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (resumen.faltaParaEnvioGratis == 0) VerdeCompletado else MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { avance },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (avance >= 1f) VerdeCompletado else DoradoBoton
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpcionEnvio(cotizacion: Cotizacion, icono: ImageVector, seleccionada: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = if (seleccionada) BorderStroke(2.dp, DoradoBoton) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) DoradoBotonContenedor else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = AzulNoche)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(cotizacion.tipoEnvio.etiqueta, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(cotizacion.plazo, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                if (cotizacion.envioGratis) "Gratis" else formatearPrecio(cotizacion.costoEnvio),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (cotizacion.envioGratis) VerdeCompletado else AzulNoche
            )
        }
    }
}

@Composable
private fun PanelTotales(resumen: ResumenCompra, onContinuar: () -> Unit) {
    Surface(shadowElevation = 10.dp, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilaTotal("Subtotal", formatearPrecio(resumen.subtotal))
            if (resumen.requiereEnvio) {
                val cotizacion = resumen.cotizacion
                FilaTotal(
                    "Envío",
                    when {
                        cotizacion == null -> "Elige un destino"
                        cotizacion.envioGratis -> "Gratis"
                        else -> formatearPrecio(cotizacion.costoEnvio)
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                AnimatedContent(targetState = resumen.total, label = "total") { total ->
                    Text(formatearPrecio(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DoradoBoton)
                }
            }
            AnimatedVisibility(visible = !resumen.listoParaPagar) {
                Text(
                    "Elige el destino del envío para continuar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onContinuar,
                enabled = resumen.listoParaPagar,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Continuar con la compra")
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
