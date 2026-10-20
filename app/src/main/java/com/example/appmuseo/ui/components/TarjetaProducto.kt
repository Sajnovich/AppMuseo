package com.example.appmuseo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.MuseoTheme
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.formatearPrecio

fun iconoDeCategoria(categoria: Categoria): ImageVector = when (categoria) {
    Categoria.MERCH -> Icons.Filled.LocalMall
    Categoria.CONSERVACION -> Icons.Filled.Inventory2
    Categoria.PUBLICACION -> Icons.Filled.Book
    Categoria.CURSO -> Icons.Filled.School
}

// ilustracion de cada producto (res/drawable-nodpi), 0 si no tiene
@DrawableRes
fun imagenDeProducto(id: Int): Int = when (id) {
    1 -> R.drawable.prod_1
    2 -> R.drawable.prod_2
    3 -> R.drawable.prod_3
    4 -> R.drawable.prod_4
    5 -> R.drawable.prod_5
    6 -> R.drawable.prod_6
    7 -> R.drawable.prod_7
    8 -> R.drawable.prod_8
    9 -> R.drawable.prod_9
    10 -> R.drawable.prod_10
    11 -> R.drawable.prod_11
    12 -> R.drawable.prod_12
    else -> 0
}

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
            contentDescription = null,
            modifier = Modifier.size(tamano * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// si el producto no tiene imagen se muestra el icono de su categoria
@Composable
fun ImagenProducto(producto: Producto, modifier: Modifier = Modifier) {
    val imagen = imagenDeProducto(producto.id)
    if (imagen != 0) {
        Image(
            painter = painterResource(imagen),
            contentDescription = "Imagen de ${producto.nombre}",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(iconoDeCategoria(producto.categoria), contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun MiniaturaProducto(producto: Producto, tamano: Dp = 56.dp) {
    ImagenProducto(
        producto = producto,
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
    )
}

// puntito verde/ambar/rojo + texto
@Composable
fun EstadoDisponibilidad(producto: Producto, modifier: Modifier = Modifier) {
    val (color, texto) = when (producto) {
        is ProductoFisico -> when {
            producto.stock == 0 -> MaterialTheme.colorScheme.error to "Agotado"
            producto.stock <= 10 -> Color(0xFFB26A00) to "Quedan ${producto.stock}"
            else -> VerdeCompletado to "En stock"
        }
        is Curso -> VerdeCompletado to "${producto.cupos} cupos"
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(5.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaProducto(
    producto: Producto,
    onVerDetalle: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Vibracion corta del telefono al agregar
    val haptic = LocalHapticFeedback.current

    Card(
        onClick = onVerDetalle,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            ImagenProducto(
                producto = producto,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            )
            Text(
                producto.categoria.etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = AzulNoche,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            if (producto.destacado) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Destacado",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(DoradoBoton)
                        .padding(4.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )
            EstadoDisponibilidad(producto)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoche,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAgregar()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = DoradoBoton, contentColor = Color.White),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Filled.AddShoppingCart, contentDescription = "Agregar ${producto.nombre} al carrito", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun TarjetaProductoPreview() {
    MuseoTheme {
        TarjetaProducto(
            producto = ProductoFisico(1, "Taza Museo de los Museos", "Taza de cerámica", 8_990, Categoria.MERCH, 25, 0.4, "MDM-001", destacado = true),
            onVerDetalle = {},
            onAgregar = {}
        )
    }
}
