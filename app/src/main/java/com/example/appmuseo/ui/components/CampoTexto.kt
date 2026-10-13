package com.example.appmuseo.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.layout.Column
import com.example.appmuseo.ui.theme.MuseoTheme
import com.example.appmuseo.ui.theme.VerdeCompletado

/**
 * CAMPO DE FORMULARIO ESTÁNDAR de toda la app (todos los formularios lo usan).
 * Así todos los campos se ven y avisan igual:
 *  - ícono a la izquierda que indica qué dato se pide,
 *  - borde rojo + ícono de error + mensaje debajo cuando el dato no es válido,
 *  - ícono verde de "listo" cuando el dato ya es correcto (retroalimentación positiva),
 *  - texto de ayuda opcional cuando no hay error.
 * La lógica de qué es válido NO está aquí: viene de Validaciones (lógica centralizada).
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    esValido: Boolean = false,
    ayuda: String? = null,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    accionFinal: (@Composable () -> Unit)? = null,   // ej: botón para mostrar la contraseña
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        leadingIcon = { Icon(icono, contentDescription = null) },
        // Ícono de la derecha. Prioridad: acción propia > error > válido
        trailingIcon = if (accionFinal != null || error != null || esValido) {
            {
                when {
                    accionFinal != null -> accionFinal()
                    error != null -> Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error en $etiqueta",
                        tint = MaterialTheme.colorScheme.error
                    )
                    else -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "$etiqueta correcto",
                        tint = VerdeCompletado
                    )
                }
            }
        } else null,
        isError = error != null,
        supportingText = if (error != null || ayuda != null) {
            {
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
                else Text(ayuda ?: "")
            }
        } else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun CampoTextoPreview() {
    MuseoTheme {
        Column {
            CampoTexto("ana@", {}, "Correo electrónico", Icons.Filled.Email, error = "Formato de correo inválido")
            CampoTexto("ana@correo.cl", {}, "Correo electrónico", Icons.Filled.Email, esValido = true)
        }
    }
}
