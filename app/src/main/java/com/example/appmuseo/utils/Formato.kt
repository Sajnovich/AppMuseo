package com.example.appmuseo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Da formato de pesos chilenos: 12990 → "$12.990".
 * Truco con colecciones (Guía 3): invertimos el texto, lo cortamos de a 3
 * dígitos (chunked), lo unimos con puntos y lo volvemos a invertir.
 */
fun formatearPrecio(valor: Int): String = "$" + separarMiles(valor)

/** 1234.6 → "1.235 km". Bajo 1 km no tiene sentido mostrar decimales. */
fun formatearDistancia(kilometros: Double): String =
    if (kilometros < 1) "menos de 1 km" else "${separarMiles(kilometros.roundToInt())} km"

/** 1234567 → "1.234.567" (separador de miles chileno). */
private fun separarMiles(valor: Int): String =
    valor.toString().reversed().chunked(3).joinToString(".").reversed()

/** Convierte milisegundos (como los guarda Room) en una fecha legible. */
fun formatearFecha(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(millis))
}
