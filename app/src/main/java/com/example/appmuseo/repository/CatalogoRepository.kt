package com.example.appmuseo.repository

import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto

/**
 * "Backend simulado" del catálogo: los datos están escritos en el mismo código.
 * Representa la "base de datos sintética de cursos y productos" que entrega el caso.
 *
 * En la Unidad 3 se reemplaza el contenido de estas funciones por llamadas
 * a la API del sitio web institucional (Retrofit); los ViewModels no cambian.
 */
class CatalogoRepository {

    private val catalogo = listOf(
        // ── Merch ──
        Producto(1, "Taza Museo de los Museos", "Taza de cerámica de 350 ml con el isotipo del museo.",
            8_990, Categoria.MERCH, stock = 25, pesoKg = 0.4, codigoBarra = "MDM-001", destacado = true),
        Producto(2, "Polera algodón orgánico", "Polera unisex con ilustración de columnas clásicas.",
            14_990, Categoria.MERCH, stock = 15, pesoKg = 0.3, codigoBarra = "MDM-002"),
        Producto(3, "Tote bag ilustrada", "Bolso de tela reutilizable con mapa de museos de Iberoamérica.",
            7_990, Categoria.MERCH, stock = 30, pesoKg = 0.2, codigoBarra = "MDM-003"),

        // ── Materiales de conservación ──
        Producto(4, "Guantes de nitrilo (caja 100)", "Para manipulación segura de piezas y documentos.",
            12_990, Categoria.CONSERVACION, stock = 20, pesoKg = 0.6, codigoBarra = "MDM-004"),
        Producto(5, "Papel libre de ácido (50 hojas)", "Papel de pH neutro para guarda de obra sobre papel.",
            18_990, Categoria.CONSERVACION, stock = 12, pesoKg = 2.5, codigoBarra = "MDM-005", destacado = true),
        Producto(6, "Caja de conservación archivística", "Caja de cartón libre de ácido para fotografías y documentos.",
            24_990, Categoria.CONSERVACION, stock = 8, pesoKg = 1.8, codigoBarra = "MDM-006"),

        // ── Publicaciones ──
        Producto(7, "Manual de Museología Contemporánea", "Libro de referencia sobre gestión y políticas de museos.",
            22_990, Categoria.PUBLICACION, stock = 10, pesoKg = 0.9, codigoBarra = "MDM-007", destacado = true),
        Producto(8, "Guía de Mediación Cultural", "Estrategias prácticas para trabajar con distintas audiencias.",
            15_990, Categoria.PUBLICACION, stock = 14, pesoKg = 0.5, codigoBarra = "MDM-008"),
        Producto(9, "Revista Audiencias & Museos N°12", "Investigación aplicada sobre públicos en Iberoamérica.",
            6_990, Categoria.PUBLICACION, stock = 40, pesoKg = 0.3, codigoBarra = "MDM-009"),

        // ── Cursos (stock = cupos, sin peso porque no se despachan) ──
        Producto(10, "Curso: Introducción a la Museología", "Online · 6 semanas · Certificado digital.",
            89_990, Categoria.CURSO, stock = 25, pesoKg = 0.0, codigoBarra = "MDM-010", destacado = true),
        Producto(11, "Curso: Conservación Preventiva", "Híbrido · 8 semanas · Incluye kit de práctica.",
            119_990, Categoria.CURSO, stock = 18, pesoKg = 0.0, codigoBarra = "MDM-011"),
        Producto(12, "Curso: Experiencias para Audiencias", "Online · 5 semanas · Diseño de visitas y mediación.",
            99_990, Categoria.CURSO, stock = 20, pesoKg = 0.0, codigoBarra = "MDM-012")
    )

    fun obtenerCatalogo(): List<Producto> = catalogo

    fun buscarPorId(id: Int): Producto? = catalogo.find { it.id == id }

    /** Búsqueda usada por el escáner. ignoreCase permite escribir "mdm-001". */
    fun buscarPorCodigo(codigo: String): Producto? =
        catalogo.find { it.codigoBarra.equals(codigo.trim(), ignoreCase = true) }
}
