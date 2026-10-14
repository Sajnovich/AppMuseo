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

/**
 * DIÁLOGO DE CONFIRMACIÓN reutilizable (principio de usabilidad: "prevenir errores").
 * Antes de una acción que no se puede deshacer (cerrar sesión, eliminar una solicitud)
 * le preguntamos al usuario si está seguro.
 *
 * Es "sin estado": quien lo usa decide cuándo mostrarlo con una variable
 * (ej: var mostrarDialogo by remember { mutableStateOf(false) }).
 *
 * @param esDestructiva si es true, el botón de confirmar se pinta con el color de error (rojo).
 */
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

/**
 * Caso particular del diálogo anterior, usado en el Perfil y en el menú lateral.
 * Así el texto del cierre de sesión se escribe una sola vez.
 */
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
