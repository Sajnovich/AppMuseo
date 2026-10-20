package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.EstadoDisponibilidad
import com.example.appmuseo.ui.components.ImagenProducto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun DetalleProductoScreen(
    productoId: Int,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    favoritosViewModel: FavoritosViewModel,
    mainViewModel: MainViewModel
) {
    val producto = catalogoViewModel.buscarPorId(productoId)
    val itemsCarrito by carritoViewModel.items.collectAsState()
    val favoritos by favoritosViewModel.productos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    if (producto == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Producto no encontrado", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    val esCurso = producto is Curso
    val esFavorito = productoId in favoritos
    // se recalcula cada vez que cambia el carrito
    val disponible = remember(itemsCarrito) { carritoViewModel.disponibleParaAgregar(producto) }
    var cantidad by rememberSaveable { mutableIntStateOf(1) }
    if (cantidad > disponible && disponible > 0) cantidad = disponible

    fun avisar(texto: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(texto)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BarraAgregar(
                producto = producto,
                cantidad = cantidad,
                disponible = disponible,
                onAgregar = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val agregadas = carritoViewModel.agregarCantidad(producto, cantidad)
                    avisar(
                        when {
                            agregadas == 0 && esCurso -> "Este curso ya está en tu carrito"
                            agregadas == 0 -> "No hay más stock disponible"
                            esCurso -> "Curso agregado. Completa tu inscripción desde el carrito"
                            else -> "$agregadas ${if (agregadas == 1) "unidad agregada" else "unidades agregadas"} al carrito"
                        }
                    )
                    cantidad = 1
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                ImagenProducto(
                    producto = producto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    BotonFlotante(onClick = { mainViewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    BotonFlotante(onClick = {
                        val guardado = favoritosViewModel.alternarProducto(productoId)
                        avisar(if (guardado) "Guardado en favoritos" else "Quitado de favoritos")
                    }) {
                        Icon(
                            if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = if (esFavorito) Color(0xFFD32F2F) else AzulNoche
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BotonFlotante(onClick = { mainViewModel.navigateTo(Screen.Carrito) }) {
                        val enCarrito = itemsCarrito.sumOf { it.cantidad }
                        BadgedBox(badge = { if (enCarrito > 0) Badge { Text("$enCarrito") } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito, $enCarrito productos")
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    producto.categoria.etiqueta.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = DoradoBoton,
                    fontWeight = FontWeight.Bold
                )
                Text(producto.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                    fontWeight = FontWeight.Bold
                )
                Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)

                // cantidad + disponibilidad lado a lado
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cantidad", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (esCurso) {
                            Text("1 cupo por persona", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            SelectorCantidad(
                                cantidad = cantidad,
                                maximo = disponible,
                                onCambiar = { cantidad = it }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Disponibilidad", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(10.dp))
                        EstadoDisponibilidad(producto)
                        if (!esCurso && disponible < producto.limiteCompra()) {
                            Text(
                                "Ya tienes ${producto.limiteCompra() - disponible} en el carrito",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                FichaTecnica(producto)

                if (!esCurso) {
                    TextButton(onClick = { mainViewModel.navigateTo(Screen.Cotizador) }) {
                        Icon(Icons.Filled.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cotizar envío a otra zona")
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonFlotante(onClick: () -> Unit, contenido: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = AzulNoche)
    ) { contenido() }
}

@Composable
private fun SelectorCantidad(cantidad: Int, maximo: Int, onCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(onClick = { onCambiar(cantidad - 1) }, enabled = cantidad > 1, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Quitar uno")
        }
        // el numero sube o baja con animacion
        AnimatedContent(
            targetState = cantidad,
            transitionSpec = {
                if (targetState > initialState) slideInVertically { it } togetherWith slideOutVertically { -it }
                else slideInVertically { -it } togetherWith slideOutVertically { it }
            },
            label = "cantidad"
        ) { valor ->
            Text(
                "$valor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center
            )
        }
        OutlinedIconButton(onClick = { onCambiar(cantidad + 1) }, enabled = cantidad < maximo, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar uno")
        }
    }
}

@Composable
private fun FichaTecnica(producto: Producto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ficha", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            when (producto) {
                is ProductoFisico -> {
                    FilaDato("Código", producto.codigoBarra)
                    HorizontalDivider()
                    FilaDato("Peso", "${producto.pesoKg} kg")
                    HorizontalDivider()
                    FilaDato("Envío", "Nacional e internacional")
                }
                is Curso -> {
                    FilaDato("Modalidad", producto.modalidad)
                    HorizontalDivider()
                    FilaDato("Duración", "${producto.semanas} semanas")
                    HorizontalDivider()
                    FilaDato("Envío", "No aplica (curso)")
                }
            }
        }
    }
}

@Composable
private fun BarraAgregar(producto: Producto, cantidad: Int, disponible: Int, onAgregar: () -> Unit) {
    val esCurso = producto is Curso
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedContent(targetState = producto.precio * cantidad, label = "total") { total ->
                    Text(formatearPrecio(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onAgregar,
                enabled = disponible > 0,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(if (esCurso) Icons.Filled.School else Icons.Filled.AddShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        disponible == 0 && esCurso -> "Ya está en tu carrito"
                        disponible == 0 -> "Sin stock disponible"
                        else -> producto.etiquetaAccion()
                    }
                )
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
