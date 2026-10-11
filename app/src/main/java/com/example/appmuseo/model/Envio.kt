package com.example.appmuseo.model

// ─────────────────────────────────────────────────────────────────────────────
// MODELOS DEL COTIZADOR DE ENVÍOS (courier simulado)
// ─────────────────────────────────────────────────────────────────────────────

enum class TipoDestino(val etiqueta: String) {
    NACIONAL("Nacional"),
    INTERNACIONAL("Internacional")
}

/** Zona de despacho con su tarifa. Funciona como "identificación de zonas" del caso. */
data class Zona(
    val nombre: String,
    val tipo: TipoDestino,
    val tarifaBase: Int,      // Costo fijo del envío (CLP)
    val costoPorKg: Int,      // Costo por cada kilo facturable (CLP)
    val diasMin: Int,
    val diasMax: Int
)

/** Resultado de una cotización. */
data class Cotizacion(
    val zona: Zona,
    val pesoFacturable: Int,
    val costoEnvio: Int,
    val envioGratis: Boolean
) {
    val plazo: String
        get() = "${zona.diasMin} a ${zona.diasMax} días hábiles"
}
