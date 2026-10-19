package com.example.appmuseo.model

// Cada tipo responde distinto a las mismas preguntas

enum class Categoria(val etiqueta: String) {
    MERCH("Merch"),
    CONSERVACION("Conservación"),
    PUBLICACION("Publicaciones"),
    CURSO("Cursos")
}

sealed class Producto {
    abstract val id: Int
    abstract val nombre: String
    abstract val descripcion: String
    abstract val precio: Int
    abstract val categoria: Categoria
    abstract val destacado: Boolean

    abstract val requiereEnvio: Boolean
    abstract val pesoKg: Double

    abstract fun limiteCompra(): Int

    abstract fun disponibilidad(): String

    open fun etiquetaAccion(): String = "Agregar al carrito"
}

data class ProductoFisico(
    override val id: Int,
    override val nombre: String,
    override val descripcion: String,
    override val precio: Int,
    override val categoria: Categoria,
    val stock: Int,
    override val pesoKg: Double,
    val codigoBarra: String,
    override val destacado: Boolean = false
) : Producto() {

    init {
        require(categoria != Categoria.CURSO) { "Un producto físico no puede ser un curso" }
    }

    override val requiereEnvio: Boolean = true
    override fun limiteCompra(): Int = stock
    override fun disponibilidad(): String = if (stock > 0) "Stock: $stock" else "Agotado"
}

data class Curso(
    override val id: Int,
    override val nombre: String,
    override val descripcion: String,
    override val precio: Int,
    val cupos: Int,
    val modalidad: String,
    val semanas: Int,
    override val destacado: Boolean = false
) : Producto() {
    override val categoria: Categoria = Categoria.CURSO
    override val requiereEnvio: Boolean = false
    override val pesoKg: Double = 0.0
    override fun limiteCompra(): Int = 1
    override fun disponibilidad(): String = "$cupos cupos · $modalidad · $semanas semanas"
    override fun etiquetaAccion(): String = "Inscribirme"
}

data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
) {
    val subtotal: Int
        get() = producto.precio * cantidad
}
