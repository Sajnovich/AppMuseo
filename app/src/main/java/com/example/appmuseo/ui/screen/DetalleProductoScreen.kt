package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.IconoCategoria
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import kotlinx.coroutines.delay

/**
 * DETALLE de un producto o curso. Recibe el id como ARGUMENTO de la ruta "detalle/{productoId}".
 */
@Composable
fun DetalleProductoScreen(
    productoId: Int,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel
) {
    val producto = catalogoViewModel.buscarPorId(productoId)
    val itemsCarrito by carritoViewModel.items.collectAsState()

    // Mensaje animado de confirmación (null = oculto)
    var mensaje by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(2_000)
            mensaje = null
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = producto?.categoria?.etiqueta ?: "Detalle",
                onVolver = { mainViewModel.navigateBack() },
                cantidadCarrito = itemsCarrito.sumOf { it.cantidad },
                onCarrito = { mainViewModel.navigateTo(Screen.Carrito) }
            )
        }
    ) { innerPadding ->
        // Control de errores: si el id no existe mostramos un aviso en vez de cerrar la app
        if (producto == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Producto no encontrado", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            DetalleContenido(
                producto = producto,
                mensaje = mensaje,
                modifier = Modifier.padding(innerPadding),
                onAgregar = {
                    val esCurso = producto is Curso
                    val agregado = carritoViewModel.agregar(producto)
                    mensaje = when {
                        agregado && esCurso -> "Curso agregado. Completa tu inscripción desde el carrito."
                        agregado -> "Agregado al carrito"
                        esCurso -> "Este curso ya está en tu carrito"
                        else -> "No hay más stock disponible"
                    }
                },
                onCotizar = { mainViewModel.navigateTo(Screen.Cotizador) }
            )
        }
    }
}

/** Contenido del detalle (separado para que la pantalla principal quede más corta). */
@Composable
private fun DetalleContenido(
    producto: Producto,
    mensaje: String?,
    modifier: Modifier,
    onAgregar: () -> Unit,
    onCotizar: () -> Unit
) {
    val esCurso = producto is Curso
    val haptic = LocalHapticFeedback.current   // vibración corta al agregar

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconoCategoria(
            categoria = producto.categoria,
            tamano = 120.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        AssistChip(onClick = {}, label = { Text(producto.categoria.etiqueta) })
        Text(producto.nombre, style = MaterialTheme.typography.headlineSmall)
        Text(
            formatearPrecio(producto.precio),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider()

        // Ficha técnica
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // "when" sobre una sealed class: el compilador exige cubrir TODAS las subclases
                when (producto) {
                    is ProductoFisico -> {
                        FilaDato("Stock", producto.stock.toString())
                        FilaDato("Código", producto.codigoBarra)
                        FilaDato("Peso", "${producto.pesoKg} kg")
                        FilaDato("Envío", "Nacional e internacional")
                    }
                    is Curso -> {
                        FilaDato("Cupos disponibles", producto.cupos.toString())
                        FilaDato("Modalidad", producto.modalidad)
                        FilaDato("Duración", "${producto.semanas} semanas")
                        FilaDato("Envío", "No aplica (curso)")
                    }
                }
            }
        }

        AnimatedVisibility(visible = mensaje != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(mensaje ?: "", color = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAgregar()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(if (esCurso) Icons.Filled.School else Icons.Filled.AddShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(producto.etiquetaAccion())   // polimorfismo: "Inscribirme" o "Agregar al carrito"
        }

        if (!esCurso) {
            OutlinedButton(
                onClick = onCotizar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cotizar envío")
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
