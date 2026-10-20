package com.example.appmuseo.model

data class CatalogoUiState(
    val productos: List<Producto> = emptyList(),
    val categoriaSeleccionada: Categoria? = null,
    val busqueda: String = ""
) {
    val productosFiltrados: List<Producto>
        get() = productos
            .filter { categoriaSeleccionada == null || it.categoria == categoriaSeleccionada }
            .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }
}

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

data class CotizadorUiState(
    val tipoDestino: TipoDestino = TipoDestino.NACIONAL,
    val zona: Zona? = null,
    val peso: String = "",
    val montoCompra: String = "",
    val resultado: Cotizacion? = null,
    val errores: CotizadorErrores = CotizadorErrores()
)

data class CotizadorErrores(
    val zona: String? = null,
    val peso: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(zona, peso).isNotEmpty()
}

data class MapaUiState(
    val museos: List<Museo> = emptyList(),
    val paisSeleccionado: String? = null,   // null = todos los países
    val tipoSeleccionado: String? = null,   // filtro "Proyecto" (arte, historia...)
    val museoSeleccionado: Museo? = null,
    val cercano: MuseoCercano? = null
) {
    val museosVisibles: List<Museo>
        get() = museos.filter {
            (paisSeleccionado == null || it.pais == paisSeleccionado) &&
                    (tipoSeleccionado == null || it.tipo == tipoSeleccionado)
        }

    val tipos: List<String>
        get() = museos.map { it.tipo }.distinct().sorted()

    // map + distinct + sorted
    val paises: List<String>
        get() = museos.map { it.pais }.distinct().sorted()
}

data class MuseoCercano(
    val museo: Museo,
    val distanciaKm: Double,
    val ubicacionAproximada: Boolean
)
