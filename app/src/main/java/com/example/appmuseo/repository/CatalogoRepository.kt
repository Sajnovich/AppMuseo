package com.example.appmuseo.repository

import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.Producto
import com.example.appmuseo.model.ProductoFisico

class CatalogoRepository {

    // LISTOF<PRODUCTO>: LA LISTA GUARDA OBJETOS DE LAS DOS
    private val catalogo: List<Producto> = listOf(
        ProductoFisico(1, "Taza Museo de los Museos", "Taza de cerámica de 350 ml con el isotipo del museo.",
            8_990, Categoria.MERCH, stock = 25, pesoKg = 0.4, codigoBarra = "MDM-001", destacado = true),
        ProductoFisico(2, "Polera algodón orgánico", "Polera unisex con ilustración de columnas clásicas.",
            14_990, Categoria.MERCH, stock = 15, pesoKg = 0.3, codigoBarra = "MDM-002"),
        ProductoFisico(3, "Tote bag ilustrada", "Bolso de tela reutilizable con mapa de museos de Iberoamérica.",
            7_990, Categoria.MERCH, stock = 30, pesoKg = 0.2, codigoBarra = "MDM-003"),

        ProductoFisico(4, "Guantes de nitrilo (caja 100)", "Para manipulación segura de piezas y documentos.",
            12_990, Categoria.CONSERVACION, stock = 20, pesoKg = 0.6, codigoBarra = "MDM-004"),
        ProductoFisico(5, "Papel libre de ácido (50 hojas)", "Papel de pH neutro para guarda de obra sobre papel.",
            18_990, Categoria.CONSERVACION, stock = 12, pesoKg = 2.5, codigoBarra = "MDM-005", destacado = true),
        ProductoFisico(6, "Caja de conservación archivística", "Caja de cartón libre de ácido para fotografías y documentos.",
            24_990, Categoria.CONSERVACION, stock = 8, pesoKg = 1.8, codigoBarra = "MDM-006"),

        ProductoFisico(7, "Manual de Museología Contemporánea", "Libro de referencia sobre gestión y políticas de museos.",
            22_990, Categoria.PUBLICACION, stock = 10, pesoKg = 0.9, codigoBarra = "MDM-007", destacado = true),
        ProductoFisico(8, "Guía de Mediación Cultural", "Estrategias prácticas para trabajar con distintas audiencias.",
            15_990, Categoria.PUBLICACION, stock = 14, pesoKg = 0.5, codigoBarra = "MDM-008"),
        ProductoFisico(9, "Revista Audiencias & Museos N°12", "Investigación aplicada sobre públicos en Iberoamérica.",
            6_990, Categoria.PUBLICACION, stock = 40, pesoKg = 0.3, codigoBarra = "MDM-009"),

        Curso(10, "Curso: Introducción a la Museología", "Fundamentos de la museología contemporánea. Certificado digital.",
            89_990, cupos = 25, modalidad = "Online", semanas = 6, destacado = true),
        Curso(11, "Curso: Conservación Preventiva", "Cuidado de colecciones. Incluye kit de práctica.",
            119_990, cupos = 18, modalidad = "Híbrido", semanas = 8),
        Curso(12, "Curso: Experiencias para Audiencias", "Diseño de visitas y mediación para distintos públicos.",
            99_990, cupos = 20, modalidad = "Online", semanas = 5)
    )

    fun obtenerCatalogo(): List<Producto> = catalogo

    fun buscarPorId(id: Int): Producto? = catalogo.find { it.id == id }

    fun obtenerProductosFisicos(): List<ProductoFisico> = catalogo.filterIsInstance<ProductoFisico>()

    fun buscarPorCodigo(codigo: String): ProductoFisico? =
        obtenerProductosFisicos().find { it.codigoBarra.equals(codigo.trim(), ignoreCase = true) }
}
