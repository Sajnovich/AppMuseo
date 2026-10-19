package com.example.appmuseo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.appmuseo.ui.theme.MuseoTheme

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    icono: ImageVector,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    esDestructiva: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCancelar,   // tocar fuera del diálogo o "atrás" = cancelar
        icon = { Icon(icono, contentDescription = null) },
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    textoConfirmar,
                    color = if (esDestructiva) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoCerrarSesion(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    DialogoConfirmacion(
        titulo = "¿Cerrar sesión?",
        mensaje = "Se vaciará tu carrito y tendrás que ingresar de nuevo.",
        icono = Icons.AutoMirrored.Filled.Logout,
        textoConfirmar = "Cerrar sesión",
        onConfirmar = onConfirmar,
        onCancelar = onCancelar,
        esDestructiva = true
    )
}

@Preview
@Composable
private fun DialogoConfirmacionPreview() {
    MuseoTheme {
        DialogoCerrarSesion(onConfirmar = {}, onCancelar = {})
    }
}
