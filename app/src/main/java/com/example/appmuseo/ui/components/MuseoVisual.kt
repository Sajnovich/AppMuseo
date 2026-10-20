package com.example.appmuseo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Museo
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton

fun iconoDeTipoMuseo(tipo: String): ImageVector = when (tipo) {
    "Arte" -> Icons.Filled.Palette
    "Historia" -> Icons.Filled.HistoryEdu
    "Arqueología" -> Icons.Filled.AccountBalance
    "Etnografía" -> Icons.Filled.Groups
    "Ciencia" -> Icons.Filled.Science
    "Ciencias naturales" -> Icons.Filled.Eco
    else -> Icons.Filled.Museum
}

// cada tipo tiene su par de colores para el degradado
private fun coloresDeTipo(tipo: String): List<Color> = when (tipo) {
    "Arte" -> listOf(Color(0xFF6A3D7C), Color(0xFFC0627A))
    "Historia" -> listOf(AzulNoche, Color(0xFF8C6A3F))
    "Arqueología" -> listOf(Color(0xFF7A4A2A), Color(0xFFC9925A))
    "Etnografía" -> listOf(Color(0xFF2E5E4E), Color(0xFFB08A3C))
    else -> listOf(Color(0xFF1F5C73), Color(0xFF3FA08A))
}

// "foto" del museo: degradado con el icono de su tipo
@Composable
fun CabeceraMuseo(
    museo: Museo,
    modifier: Modifier = Modifier,
    tamanoIcono: Dp = 96.dp,
    contenido: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.background(Brush.linearGradient(coloresDeTipo(museo.tipo))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            iconoDeTipoMuseo(museo.tipo),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(tamanoIcono)
        )
        contenido()
    }
}

@Composable
fun MiniaturaMuseo(museo: Museo, tamano: Dp = 56.dp) {
    CabeceraMuseo(
        museo = museo,
        tamanoIcono = tamano * 0.55f,
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
    )
}

@Composable
fun EtiquetaMuseo(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun Estrellas(calificacion: Double, resenas: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val icono = when {
                calificacion >= i -> Icons.Filled.Star
                calificacion >= i - 0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarOutline
            }
            Icon(icono, contentDescription = null, tint = DoradoBoton, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            "$calificacion ($resenas reseñas)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EtiquetasMuseo(museo: Museo, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        museo.etiquetas().forEach {
            EtiquetaMuseo(it)
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}
