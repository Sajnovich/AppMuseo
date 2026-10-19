package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.IconoCategoria
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel

/** CARRITO: lista de productos/cursos elegidos, cantidades y subtotal. */
@Composable
fun CarritoScreen(carritoViewModel: CarritoViewModel, mainViewModel: MainViewModel) {
    // Al observar "items", la pantalla se vuelve a dibujar cada vez que cambia el carrito
    val items by carritoViewModel.items.collectAsState()
    val subtotal = items.sumOf { it.subtotal }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mi carrito", onVolver = { mainViewModel.navigateBack() }) },
        bottomBar = {
            if (items.isNotEmpty()) {
                // Surface da un fondo elevado; navigationBarsPadding evita quedar bajo la barra del sistema
                Surface(tonalElevation = 3.dp) {
                    Column(modifier = Modifier.navigationBarsPadding().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.titleMedium)
                            // El total cambia con un fundido suave cada vez que se modifica el carrito
                            AnimatedContent(targetState = subtotal, label = "subtotal") { total ->
                                Text(formatearPrecio(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (carritoViewModel.requiereEnvio()) {
                            Text(
                                if (subtotal >= CotizadorEnvio.MONTO_ENVIO_GRATIS) "¡Tienes envío nacional gratis!"
                                else "Envío nacional gratis desde ${formatearPrecio(CotizadorEnvio.MONTO_ENVIO_GRATIS)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { mainViewModel.navigateTo(Screen.Solicitud) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Continuar con la solicitud")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            // Estado vacío: explicamos qué hacer en vez de mostrar una pantalla en blanco (UX)
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { mainViewModel.navegarPestana(Screen.Catalogo) }) {
                    Text("Ver catálogo")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.producto.id }) { item ->
                    FilaCarrito(
                        item = item,
                        modifier = Modifier.animateItem(),   // al eliminar, la fila desaparece con animación
                        onSumar = { carritoViewModel.agregar(item.producto) },
                        onRestar = { carritoViewModel.disminuir(item.producto.id) },
                        onEliminar = { carritoViewModel.eliminar(item.producto.id) }
                    )
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
    val esCurso = item.producto is Curso

    Card(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconoCategoria(item.producto.categoria, tamano = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.producto.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(formatearPrecio(item.subtotal), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                if (!esCurso) {
                    // Controles de cantidad (los cursos son siempre 1 inscripción)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRestar) { Icon(Icons.Filled.Remove, contentDescription = "Quitar una unidad") }
                        // AnimatedContent: el número "rueda" hacia arriba al sumar y hacia abajo al restar
                        AnimatedContent(
                            targetState = item.cantidad,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically { altura -> altura } + fadeIn()) togetherWith
                                            (slideOutVertically { altura -> -altura } + fadeOut())
                                } else {
                                    (slideInVertically { altura -> -altura } + fadeIn()) togetherWith
                                            (slideOutVertically { altura -> altura } + fadeOut())
                                }
                            },
                            label = "cantidad"
                        ) { cantidad ->
                            Text("$cantidad", style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = onSumar) { Icon(Icons.Filled.Add, contentDescription = "Agregar una unidad") }
                    }
                } else {
                    Text("Inscripción", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${item.producto.nombre}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
