package com.example.appmuseo.model

// ─────────────────────────────────────────────────────────────────────────────
// CATÁLOGO UNIFICADO con HERENCIA y POLIMORFISMO (Guía 4: POO)
//
//              Producto  (sealed class = clase base "cerrada")
//              /       \
//   ProductoFisico      Curso
//   (se despacha)       (no se despacha, se inscribe)
//
// Cada tipo responde distinto a las mismas preguntas (limiteCompra, disponibilidad,
// etiquetaAccion...). Así el resto de la app NO necesita preguntar "¿es un curso?"
// con if: llama al método y cada objeto sabe qué hacer. Eso es polimorfismo.
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
 * Clase BASE de todo lo que se vende en la app.
 * "sealed" = solo puede tener las subclases declaradas en este archivo, por eso un
 * "when (producto)" sabe que las únicas opciones son ProductoFisico o Curso.
 */
sealed class Producto {
    // Propiedades comunes (cada subclase las entrega en su constructor)
    abstract val id: Int
    abstract val nombre: String
    abstract val descripcion: String
    abstract val precio: Int          // Pesos chilenos (CLP), por eso Int y no Double
    abstract val categoria: Categoria
    abstract val destacado: Boolean

    // Comportamiento que cada subclase define a su manera (polimorfismo)
    abstract val requiereEnvio: Boolean
    abstract val pesoKg: Double

    /** Máximo de unidades que se pueden agregar al carrito. */
    abstract fun limiteCompra(): Int

    /** Texto corto de disponibilidad para las tarjetas. */
    abstract fun disponibilidad(): String

    /** Texto del botón principal. "open" = tiene valor por defecto, pero se puede sobrescribir. */
    open fun etiquetaAccion(): String = "Agregar al carrito"
}

/** Producto que se despacha: merch, materiales de conservación y publicaciones. */
data class ProductoFisico(
    override val id: Int,
    override val nombre: String,
    override val descripcion: String,
    override val precio: Int,
    override val categoria: Categoria,
    val stock: Int,
    override val pesoKg: Double,
    val codigoBarra: String,          // Código sintético que "lee" el escáner simulado
    override val destacado: Boolean = false
) : Producto() {

    init {
        // Regla de negocio: un producto físico nunca puede ser de la categoría "Cursos"
        require(categoria != Categoria.CURSO) { "Un producto físico no puede ser un curso" }
    }

    override val requiereEnvio: Boolean = true
    override fun limiteCompra(): Int = stock
    override fun disponibilidad(): String = if (stock > 0) "Stock: $stock" else "Agotado"
}

/** Curso o programa formativo: no tiene peso ni se despacha. */
data class Curso(
    override val id: Int,
    override val nombre: String,
    override val descripcion: String,
    override val precio: Int,
    val cupos: Int,
    val modalidad: String,            // Online, Híbrido, Presencial
    val semanas: Int,
    override val destacado: Boolean = false
) : Producto() {
    override val categoria: Categoria = Categoria.CURSO
    override val requiereEnvio: Boolean = false
    override val pesoKg: Double = 0.0
    override fun limiteCompra(): Int = 1                 // una inscripción por persona
    override fun disponibilidad(): String = "$cupos cupos · $modalidad · $semanas semanas"
    override fun etiquetaAccion(): String = "Inscribirme"
}

/** Una línea del carrito: qué producto y cuántas unidades. */
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Int
        get() = producto.precio * cantidad
}
