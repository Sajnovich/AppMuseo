package com.example.appmuseo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Partimos de la tipografía por defecto de Material 3 y cambiamos los títulos a una
// fuente con serifa (look "editorial / museo"). Los tamaños se miden en "sp", así que
// respetan el tamaño de letra que el usuario configuró en su teléfono (accesibilidad).
private val base = Typography()

val Typography = Typography(
    headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
)
