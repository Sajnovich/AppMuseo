package com.example.appmuseo.utils

/**
 * Convierte coordenadas (latitud, longitud) en una posición RELATIVA (0..1) dentro
 * del rectángulo del mapa simulado. Así podemos dibujar los marcadores sin usar
 * Google Maps (que requiere API key; se verá con el backend en la Unidad 3).
 *
 *   x = 0 → borde oeste      x = 1 → borde este
 *   y = 0 → borde norte      y = 1 → borde sur
 */
object ProyeccionMapa {

    // Rectángulo que cubre Iberoamérica (desde México hasta Patagonia, más España y Portugal)
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
