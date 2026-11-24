package com.example.appmuseo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonContenedor

// Piezas comunes de las pantallas de gestión (Administrador e Investigador)

// fila de una lista de gestión: ícono, título, detalle y acciones (editar / eliminar)
@Composable
fun TarjetaGestion(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    etiqueta: String? = null,
    colorEtiqueta: Color = DoradoBoton,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(DoradoBotonContenedor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icono, contentDescription = null, tint = AzulNoche) }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (etiqueta != null) {
                    Text(
                        etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(colorEtiqueta, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            acciones()
        }
    }
}

// aviso de resultado (ej: "Se agregó al catálogo"), se cierra con la X
@Composable
fun MensajeGestion(mensaje: String?, onCerrar: () -> Unit) {
    AnimatedVisibility(visible = mensaje != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AzulNoche),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White)
                Text(mensaje ?: "", color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp))
                IconButton(onClick = onCerrar) { Icon(Icons.Filled.Close, contentDescription = "Cerrar aviso", tint = Color.White) }
            }
        }
    }
}

// recuerda que la acción va protegida con el token del rol
@Composable
fun AvisoRolJwt(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DoradoBotonContenedor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AzulNoche, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = AzulNoche, modifier = Modifier.padding(start = 8.dp))
    }
}

// fila de chips para elegir una opción (categoría, tipo, estado...)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChipsOpciones(opciones: List<T>, seleccionada: T?, etiqueta: (T) -> String, onElegir: (T) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onElegir(opcion) },
                label = { Text(etiqueta(opcion)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DoradoBotonContenedor)
            )
        }
    }
}
