package com.example.appmuseo.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appmuseo.ui.theme.MuseoTheme

/**
 * IMAGEN INTELIGENTE (Guía 13): muestra una imagen circular a partir de un Uri.
 * - Si hay Uri → la carga con Coil (AsyncImage), que lee la imagen sin bloquear la pantalla.
 * - Si no hay Uri → muestra un ícono por defecto.
 * Se reutiliza en el Perfil (foto de usuario) y en el Escáner (foto del producto).
 */
@Composable
fun ImagenInteligente(
    uri: Uri?,
    modifier: Modifier = Modifier,
    tamano: Dp = 140.dp,
    descripcion: String = "Foto de perfil",
    iconoPorDefecto: ImageVector = Icons.Filled.AccountCircle
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,   // recorta para llenar el círculo
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = iconoPorDefecto,
                contentDescription = "$descripcion: sin imagen",
                modifier = Modifier.size(tamano * 0.7f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImagenInteligentePreview() {
    MuseoTheme { ImagenInteligente(uri = null) }
}
