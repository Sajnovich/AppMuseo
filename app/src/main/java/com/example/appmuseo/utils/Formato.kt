package com.example.appmuseo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Da formato de pesos chilenos: 12990 → "$12.990".
 * Truco con colecciones (Guía 3): invertimos el texto, lo cortamos de a 3
 * dígitos (chunked), lo unimos con puntos y lo volvemos a invertir.
 */
fun formatearPrecio(valor: Int): String {
    val conPuntos = valor.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$$conPuntos"
}

/** Convierte milisegundos (como los guarda Room) en una fecha legible. */
fun formatearFecha(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(millis))
}
