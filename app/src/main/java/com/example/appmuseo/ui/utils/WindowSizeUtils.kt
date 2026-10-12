package com.example.appmuseo.ui.utils

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

/** Calcula si la pantalla es Compact, Medium o Expanded (Guía 9: Window Size Classes). */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun obtenerWindowSizeClass(): WindowSizeClass {
    return calculateWindowSizeClass(LocalActivity.current as Activity)
}

/**
 * Cantidad de columnas según el ancho de la pantalla:
 * teléfono vertical = 1, teléfono horizontal / tablet pequeña = 2, tablet grande = 3.
 */
@Composable
fun columnasSegunPantalla(): Int {
    return when (obtenerWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }
}
