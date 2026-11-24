package com.example.appmuseo.utils

import com.example.appmuseo.model.FormMuseo
import com.example.appmuseo.model.FormProducto
import com.example.appmuseo.model.numero

// Validaciones de los formularios de gestión, antes de mandar al backend
// (el backend vuelve a validar con @Valid: si algo se escapa, responde 400 con el campo)
object ValidacionesGestion {

    private val CODIGO = Regex("^MDM-\\d{3}$")

    fun validarProducto(f: FormProducto): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (f.nombre.isBlank()) errores["nombre"] = "El nombre es obligatorio"
        else if (f.nombre.trim().length > 120) errores["nombre"] = "Máximo 120 caracteres"
        if (f.descripcion.isBlank()) errores["descripcion"] = "La descripción es obligatoria"
        else if (f.descripcion.trim().length > 255) errores["descripcion"] = "Máximo 255 caracteres"

        val precio = f.precio.trim().toIntOrNull()
        if (precio == null || precio <= 0) errores["precio"] = "Ingresa un precio mayor a 0"

        val stock = f.stock.trim().toIntOrNull()
        if (stock == null || stock < 0) errores["stock"] = if (f.esCurso) "Ingresa los cupos (0 o más)" else "Ingresa el stock (0 o más)"

        if (f.esCurso) {
            val semanas = f.semanas.trim().toIntOrNull()
            if (semanas == null || semanas <= 0) errores["semanas"] = "Indica la duración en semanas"
            if (f.modalidad.isBlank()) errores["modalidad"] = "Indica la modalidad"
        } else {
            val peso = numero(f.pesoKg)
            if (peso == null || peso < 0 || peso > 30) errores["pesoKg"] = "Peso entre 0 y 30 kg"
            if (f.codigoBarra.isNotBlank() && !CODIGO.matches(f.codigoBarra.trim().uppercase())) {
                errores["codigoBarra"] = "Formato MDM-000"
            }
        }
        return errores
    }

    fun validarMuseo(f: FormMuseo): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (f.nombre.isBlank()) errores["nombre"] = "El nombre es obligatorio"
        if (f.pais.isBlank()) errores["pais"] = "El país es obligatorio"
        if (f.ciudad.isBlank()) errores["ciudad"] = "La ciudad es obligatoria"
        if (f.tipo.isBlank()) errores["tipo"] = "Elige el tipo de museo"
        if (f.descripcion.isBlank()) errores["descripcion"] = "La descripción es obligatoria"
        else if (f.descripcion.trim().length > 255) errores["descripcion"] = "Máximo 255 caracteres"

        val lat = numero(f.latitud)
        if (lat == null || lat < -90 || lat > 90) errores["latitud"] = "Latitud entre -90 y 90"
        val lon = numero(f.longitud)
        if (lon == null || lon < -180 || lon > 180) errores["longitud"] = "Longitud entre -180 y 180"

        val anio = f.anio.trim().toIntOrNull()
        if (anio == null || anio < 2000 || anio > 2100) errores["anio"] = "Año entre 2000 y 2100"
        val nota = numero(f.calificacion)
        if (nota == null || nota < 0 || nota > 5) errores["calificacion"] = "Calificación entre 0 y 5"
        val resenas = f.resenas.trim().toIntOrNull()
        if (resenas == null || resenas < 0) errores["resenas"] = "Reseñas: 0 o más"
        return errores
    }
}
