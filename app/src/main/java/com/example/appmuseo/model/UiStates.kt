package com.example.appmuseo.model

// ─────────────────────────────────────────────────────────────────────────────
// ESTADOS DE PANTALLA (UiState). Cada pantalla "dibuja" lo que hay en su estado.
// ─────────────────────────────────────────────────────────────────────────────

/** Estado del catálogo: la lista completa + los filtros activos. */
data class CatalogoUiState(
    val productos: List<Producto> = emptyList(),
    val categoriaSeleccionada: Categoria? = null,   // null = "Todos"
    val busqueda: String = ""
) {
    // Lista que se muestra en pantalla. Se calcula con filter (Guía 3: colecciones)
    val productosFiltrados: List<Producto>
        get() = productos
            .filter { categoriaSeleccionada == null || it.categoria == categoriaSeleccionada }
            .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }
}

/** Estado del formulario de solicitud de compra / inscripción (Guía 11). */
data class SolicitudUiState(
    val nombre: String = "",
    val correo: String = "",
    val direccion: String = "",
    val tipoDestino: TipoDestino = TipoDestino.NACIONAL,
    val zona: Zona? = null,
    val cotizacion: Cotizacion? = null,
    val aceptaTerminos: Boolean = false,
    val errores: SolicitudErrores = SolicitudErrores()
)

data class SolicitudErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val direccion: String? = null,
    val zona: String? = null,
    val terminos: String? = null
) {
    fun hayErrores(): Boolean =
        listOfNotNull(nombre, correo, direccion, zona, terminos).isNotEmpty()
}

/** Estado del cotizador. Peso y monto se guardan como texto porque vienen de un TextField. */
data class CotizadorUiState(
    val tipoDestino: TipoDestino = TipoDestino.NACIONAL,
    val zona: Zona? = null,
    val peso: String = "",
    val montoCompra: String = "",
    val resultado: Cotizacion? = null,
    val errores: CotizadorErrores = CotizadorErrores()
)

/** Un error por campo del cotizador (mismo patrón que los demás formularios). */
data class CotizadorErrores(
    val zona: String? = null,
    val peso: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(zona, peso).isNotEmpty()
}

/** Estado del mapa de museos. */
data class MapaUiState(
    val museos: List<Museo> = emptyList(),
    val paisSeleccionado: String? = null,    // null = todos los países
    val museoSeleccionado: Museo? = null
) {
    val museosVisibles: List<Museo>
        get() = if (paisSeleccionado == null) museos else museos.filter { it.pais == paisSeleccionado }

    // map + distinct + sorted: lista de países sin repetir y ordenada
    val paises: List<String>
        get() = museos.map { it.pais }.distinct().sorted()
}
