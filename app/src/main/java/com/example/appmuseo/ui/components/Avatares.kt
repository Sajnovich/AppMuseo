package com.example.appmuseo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// avatares ficticios: un icono con un color de fondo
private val avatares: List<Pair<ImageVector, Color>> = listOf(
    Icons.Filled.Person to Color(0xFF5C7AA6),
    Icons.Filled.Face to Color(0xFFB0705A),
    Icons.Filled.Mood to Color(0xFF6E9A6B),
    Icons.Filled.School to Color(0xFF15294A),
    Icons.Filled.EmojiEmotions to Color(0xFFAD8B4E)
)

val cantidadAvatares: Int get() = avatares.size

@Composable
fun AvatarCirculo(indice: Int, tamano: Dp = 56.dp, modifier: Modifier = Modifier) {
    val (icono, color) = avatares[indice.coerceIn(0, avatares.lastIndex)]
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(tamano * 0.6f))
    }
}

/** fila para elegir avatar; el elegido se agranda y lleva borde dorado */
@Composable
fun SelectorAvatar(seleccionado: Int, onSeleccionar: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        avatares.indices.forEach { i ->
            val elegido = i == seleccionado
            val tamano by animateDpAsState(if (elegido) 60.dp else 48.dp, label = "avatar")
            AvatarCirculo(
                indice = i,
                tamano = tamano,
                modifier = Modifier
                    .border(
                        width = if (elegido) 3.dp else 0.dp,
                        color = if (elegido) MaterialTheme.colorScheme.secondary else Color.Transparent,
                        shape = CircleShape
                    )
                    .padding(if (elegido) 3.dp else 0.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.RadioButton) { onSeleccionar(i) }
                    .semantics {
                        contentDescription = "Avatar ${i + 1}"
                        selected = elegido
                    }
            )
        }
    }
}
