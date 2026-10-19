package com.example.appmuseo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun formatearPrecio(valor: Int): String = "$" + separarMiles(valor)

fun formatearDistancia(kilometros: Double): String =
    if (kilometros < 1) "menos de 1 km" else "${separarMiles(kilometros.roundToInt())} km"

private fun separarMiles(valor: Int): String =
    valor.toString().reversed().chunked(3).joinToString(".").reversed()

fun formatearFecha(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(millis))
}
