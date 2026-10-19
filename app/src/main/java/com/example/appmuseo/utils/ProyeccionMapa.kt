package com.example.appmuseo.utils

object ProyeccionMapa {

    const val LAT_NORTE = 45.0
    const val LAT_SUR = -57.0
    const val LON_OESTE = -120.0
    const val LON_ESTE = 5.0

    fun aPosicionRelativa(latitud: Double, longitud: Double): Pair<Float, Float> {
        val x = ((longitud - LON_OESTE) / (LON_ESTE - LON_OESTE)).coerceIn(0.0, 1.0)
        val y = ((LAT_NORTE - latitud) / (LAT_NORTE - LAT_SUR)).coerceIn(0.0, 1.0)
        return Pair(x.toFloat(), y.toFloat())
    }
}
