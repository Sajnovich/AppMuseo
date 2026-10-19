package com.example.appmuseo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulNoche,
    onPrimary = Color.White,
    primaryContainer = AzulNocheContenedor,
    onPrimaryContainer = AzulNocheOscuro,
    secondary = Terracota,
    onSecondary = Color.White,
    secondaryContainer = TerracotaContenedor,
    onSecondaryContainer = TerracotaOscuro,
    tertiary = Dorado,
    onTertiary = Color.White,
    tertiaryContainer = DoradoContenedor,
    onTertiaryContainer = DoradoOscuro,
    background = Marfil,
    onBackground = TextoPrincipal,
    surface = Marfil,
    onSurface = TextoPrincipal,
    surfaceVariant = PiedraClara,
    onSurfaceVariant = TextoSecundario
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaro,
    onPrimary = AzulNoche,
    primaryContainer = AzulProfundo,
    onPrimaryContainer = AzulNocheContenedor,
    secondary = TerracotaClara,
    onSecondary = TerracotaOscuro,
    secondaryContainer = TerracotaProfunda,
    onSecondaryContainer = TerracotaContenedor,
    tertiary = DoradoClaro,
    onTertiary = DoradoOscuro,
    tertiaryContainer = DoradoProfundo,
    onTertiaryContainer = DoradoContenedor,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = FondoOscuro,
    onSurface = TextoClaro,
    surfaceVariant = PiedraOscura,
    onSurfaceVariant = TextoClaroSecundario
)

@Composable
fun MuseoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
