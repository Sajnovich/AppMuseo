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

data class Cotizacion(
    val zona: Zona,
    val pesoFacturable: Int,
    val costoEnvio: Int,
    val envioGratis: Boolean
) {
    val plazo: String
        get() = "${zona.diasMin} a ${zona.diasMax} días hábiles"
}
