package com.example.appmuseo.model

// ─────────────────────────────────────────────────────────────────────────────
// CATÁLOGO UNIFICADO: productos físicos y cursos viven en la misma estructura.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Categorías del catálogo. Un "enum class" representa un conjunto FIJO de valores,
 * así evitamos errores de tipeo como "merh" o "Curso " (con espacio).
 */
enum class Categoria(val etiqueta: String) {
    MERCH("Merch"),
    CONSERVACION("Conservación"),
    PUBLICACION("Publicaciones"),
    CURSO("Cursos")
}

/**
 * Un producto o curso del catálogo.
 * "data class" = clase pensada para guardar datos (trae equals, toString y copy).
 */
data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Int,          // Pesos chilenos (CLP), por eso usamos Int y no Double
    val categoria: Categoria,
    val stock: Int,           // En los cursos representa los CUPOS disponibles
    val pesoKg: Double,       // 0.0 en cursos, porque no se despachan
    val codigoBarra: String,  // Código sintético que "lee" el escáner simulado
    val destacado: Boolean = false
) {
    // Propiedad calculada: no se guarda, se obtiene a partir de la categoría
    val requiereEnvio: Boolean
        get() = categoria != Categoria.CURSO
}

/** Una línea del carrito: qué producto y cuántas unidades. */
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Int
        get() = producto.precio * cantidad
}
