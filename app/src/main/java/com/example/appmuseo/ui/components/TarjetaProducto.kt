package com.example.appmuseo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.ui.theme.MuseoTheme
import com.example.appmuseo.utils.formatearPrecio

/** Ícono que representa cada categoría (when = equivalente moderno del switch). */
fun iconoDeCategoria(categoria: Categoria): ImageVector = when (categoria) {
    Categoria.MERCH -> Icons.Filled.LocalMall
    Categoria.CONSERVACION -> Icons.Filled.Inventory2
    Categoria.PUBLICACION -> Icons.Filled.Book
    Categoria.CURSO -> Icons.Filled.School
}

/** Cuadro de color con el ícono de la categoría (reemplaza la foto del producto en este MVP). */
@Composable
fun IconoCategoria(categoria: Categoria, modifier: Modifier = Modifier, tamano: Dp = 56.dp) {
    val colorFondo = when (categoria) {
        Categoria.MERCH -> MaterialTheme.colorScheme.secondaryContainer
        Categoria.CONSERVACION -> MaterialTheme.colorScheme.tertiaryContainer
        Categoria.PUBLICACION -> MaterialTheme.colorScheme.primaryContainer
        Categoria.CURSO -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconoDeCategoria(categoria),
            contentDescription = null,   // decorativo: la categoría ya aparece escrita
            modifier = Modifier.size(tamano * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * TARJETA DE PRODUCTO reutilizable: se usa en el catálogo y en los destacados del inicio.
 * Recibe "eventos" (onVerDetalle, onAgregar) en vez de un ViewModel: así el componente
 * no depende de ninguna pantalla y se puede previsualizar fácilmente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaProducto(
    producto: Producto,
    onVerDetalle: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Vibración corta del teléfono al agregar: retroalimentación física de la acción
    val haptic = LocalHapticFeedback.current

    Card(
        onClick = onVerDetalle,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoCategoria(producto.categoria)
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = producto.categoria.etiqueta.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = producto.disponibilidad(),   // polimorfismo: cada subclase arma su texto
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalIconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAgregar()
            }) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = "Agregar ${producto.nombre} al carrito")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaProductoPreview() {
    MuseoTheme {
        TarjetaProducto(
            producto = ProductoFisico(1, "Taza Museo de los Museos", "Taza de cerámica", 8_990, Categoria.MERCH, 25, 0.4, "MDM-001"),
            onVerDetalle = {},
            onAgregar = {}
        )
    }
}
