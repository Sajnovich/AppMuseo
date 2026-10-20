package com.example.appmuseo.model

enum class TipoDestino(val etiqueta: String) {
    NACIONAL("Nacional"),
    INTERNACIONAL("Internacional")
}

data class Zona(
    val nombre: String,
    val tipo: TipoDestino,
    val tarifaBase: Int,
    val costoPorKg: Int,
    val diasMin: Int,
    val diasMax: Int
)

// express cuesta mas pero llega en la mitad del tiempo
enum class TipoEnvio(val etiqueta: String, val recargo: Double) {
    ESTANDAR("Estándar", 1.0),
    EXPRESS("Express", 1.6)
}

data class Cotizacion(
    val zona: Zona,
    val pesoFacturable: Int,
    val costoEnvio: Int,
    val envioGratis: Boolean,
    val tipoEnvio: TipoEnvio = TipoEnvio.ESTANDAR
) {
    val diasMin: Int get() = if (tipoEnvio == TipoEnvio.EXPRESS) (zona.diasMin / 2).coerceAtLeast(1) else zona.diasMin
    val diasMax: Int get() = if (tipoEnvio == TipoEnvio.EXPRESS) (zona.diasMax / 2).coerceAtLeast(1) else zona.diasMax

    val plazo: String
        get() = if (diasMin == diasMax) "$diasMin día hábil" else "$diasMin a $diasMax días hábiles"
}
