package com.example.appmuseo.model

import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.MuseoRequestDto
import com.example.appmuseo.data.remote.dto.PedidoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.data.remote.dto.ProductoRequestDto

// Formulario de alta / modificación de un producto o curso (Administrador).
// Los números se guardan como texto mientras se escriben y se convierten al guardar
data class FormProducto(
    val id: Long? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val categoria: Categoria = Categoria.MERCH,
    val stock: String = "",
    val pesoKg: String = "",
    val codigoBarra: String = "",
    val modalidad: String = "Online",
    val semanas: String = "",
    val destacado: Boolean = false
) {
    val esCurso: Boolean get() = categoria == Categoria.CURSO
    val esNuevo: Boolean get() = id == null

    fun aRequest() = ProductoRequestDto(
        nombre = nombre.trim(),
        descripcion = descripcion.trim(),
        precio = precio.trim().toIntOrNull() ?: 0,
        categoria = categoria.name,
        stock = stock.trim().toIntOrNull() ?: 0,
        // un curso no se despacha: no tiene peso ni código de barra
        pesoKg = if (esCurso) 0.0 else numero(pesoKg) ?: 0.0,
        codigoBarra = if (esCurso) null else codigoBarra.trim().uppercase().ifBlank { null },
        modalidad = if (esCurso) modalidad.trim() else null,
        semanas = if (esCurso) semanas.trim().toIntOrNull() else null,
        destacado = destacado
    )

    companion object {
        fun desde(p: ProductoDto) = FormProducto(
            id = p.id,
            nombre = p.nombre,
            descripcion = p.descripcion,
            precio = p.precio.toString(),
            categoria = Categoria.entries.find { it.name == p.categoria } ?: Categoria.MERCH,
            stock = p.stock.toString(),
            pesoKg = p.pesoKg.toString(),
            codigoBarra = p.codigoBarra ?: "",
            modalidad = p.modalidad ?: "Online",
            semanas = p.semanas?.toString() ?: "",
            destacado = p.destacado
        )
    }
}

// Formulario de una ficha museológica (Investigador / Administrador)
data class FormMuseo(
    val id: Long? = null,
    val nombre: String = "",
    val pais: String = "",
    val ciudad: String = "",
    val tipo: String = "Historia",
    val descripcion: String = "",
    val detalle: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val anio: String = "2026",
    val calificacion: String = "4.5",
    val resenas: String = "0"
) {
    val esNuevo: Boolean get() = id == null

    fun aRequest() = MuseoRequestDto(
        nombre = nombre.trim(),
        pais = pais.trim(),
        ciudad = ciudad.trim(),
        tipo = tipo.trim(),
        descripcion = descripcion.trim(),
        detalle = detalle.trim(),
        latitud = numero(latitud) ?: 0.0,
        longitud = numero(longitud) ?: 0.0,
        anioLevantamiento = anio.trim().toIntOrNull() ?: 0,
        calificacion = numero(calificacion) ?: 0.0,
        resenas = resenas.trim().toIntOrNull() ?: 0
    )

    companion object {
        val TIPOS = listOf("Arte", "Historia", "Arqueología", "Ciencia")

        fun desde(m: MuseoDto) = FormMuseo(
            id = m.id,
            nombre = m.nombre,
            pais = m.pais,
            ciudad = m.ciudad,
            tipo = m.tipo,
            descripcion = m.descripcion,
            detalle = m.detalle ?: "",
            latitud = m.latitud.toString(),
            longitud = m.longitud.toString(),
            anio = m.anioLevantamiento.toString(),
            calificacion = m.calificacion.toString(),
            resenas = m.resenas.toString()
        )
    }
}

// acepta "4,5" o "4.5"
internal fun numero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

// Estado de las pantallas de gestión
data class GestionUiState(
    val productos: List<ProductoDto> = emptyList(),
    val museos: List<MuseoDto> = emptyList(),
    val pedidos: List<PedidoDto> = emptyList(),
    val formProducto: FormProducto = FormProducto(),
    val formMuseo: FormMuseo = FormMuseo(),
    val errores: Map<String, String> = emptyMap(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val filtroEstado: String? = null
) {
    // solicitudes visibles según el filtro de estado elegido (null = todas)
    val pedidosFiltrados: List<PedidoDto>
        get() = if (filtroEstado == null) pedidos else pedidos.filter { it.estado == filtroEstado }

    val pendientes: Int get() = pedidos.count { it.estado == "PENDIENTE" }
}
