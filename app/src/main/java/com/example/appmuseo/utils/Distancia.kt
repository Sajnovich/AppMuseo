package com.example.appmuseo.utils

import com.example.appmuseo.model.Museo
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object Distancia {

    private const val RADIO_TIERRA_KM = 6371.0

    fun kilometrosEntre(latitud1: Double, longitud1: Double, latitud2: Double, longitud2: Double): Double {
        val difLatitud = Math.toRadians(latitud2 - latitud1)
        val difLongitud = Math.toRadians(longitud2 - longitud1)
        val a = sin(difLatitud / 2).pow(2) +
                cos(Math.toRadians(latitud1)) * cos(Math.toRadians(latitud2)) * sin(difLongitud / 2).pow(2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
    }

    fun museoMasCercano(museos: List<Museo>, latitud: Double, longitud: Double): Museo? =
        museos.minByOrNull { museo -> kilometrosEntre(latitud, longitud, museo.latitud, museo.longitud) }
}
