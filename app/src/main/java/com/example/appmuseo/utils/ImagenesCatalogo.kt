package com.example.appmuseo.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

// Imagen que el Administrador / Investigador dejó para un producto o museo
sealed interface ImagenGuardada {
    data class Propia(val ruta: String) : ImagenGuardada   // foto de cámara o galería
    data object Quitada : ImagenGuardada                    // se borró la imagen que traía
}

// Lo que el ViewModel necesita para guardar imágenes (en las pruebas se reemplaza por un mock)
interface AlmacenImagenes {
    suspend fun guardar(clave: String, uri: String)
    suspend fun quitar(clave: String)
    suspend fun olvidar(clave: String)
}

fun claveProducto(id: Number) = "producto_${id.toLong()}"
fun claveMuseo(id: Number) = "museo_${id.toLong()}"

// ¿tiene imagen para mostrar? la propia, o la ilustración que trae la app si no se quitó
fun tieneImagen(guardada: ImagenGuardada?, traePorDefecto: Boolean): Boolean = when (guardada) {
    is ImagenGuardada.Propia -> true
    ImagenGuardada.Quitada -> false
    null -> traePorDefecto
}

// Las imágenes se guardan en la carpeta privada de la app (filesDir/catalogo), igual que la
// foto de perfil. El backend guarda los datos (nombre, precio, stock...) y el teléfono la imagen.
//   producto_5_1764000000.jpg -> imagen propia del producto 5
//   producto_5.quitada        -> el producto 5 quedó sin imagen
object ImagenesCatalogo : AlmacenImagenes {

    private var carpeta: File? = null
    private var resolver: ContentResolver? = null

    // StateFlow: las pantallas se redibujan solas cuando cambia una imagen
    private val _imagenes = MutableStateFlow<Map<String, ImagenGuardada>>(emptyMap())
    val imagenes: StateFlow<Map<String, ImagenGuardada>> = _imagenes

    fun iniciar(context: Context) {
        val app = context.applicationContext
        val dir = File(app.filesDir, "catalogo").apply { mkdirs() }
        carpeta = dir
        resolver = app.contentResolver
        _imagenes.value = leerNombres(dir.list()?.toList() ?: emptyList(), dir.absolutePath)
    }

    // traduce los nombres de archivo de la carpeta a un mapa clave -> imagen
    internal fun leerNombres(nombres: List<String>, ruta: String): Map<String, ImagenGuardada> =
        nombres.mapNotNull { nombre ->
            when {
                nombre.endsWith(".quitada") -> nombre.removeSuffix(".quitada") to ImagenGuardada.Quitada
                nombre.endsWith(".jpg") -> nombre.substringBeforeLast('_') to ImagenGuardada.Propia("$ruta/$nombre")
                else -> null
            }
        }.toMap()

    // copia la foto (de cámara o galería) a la carpeta privada; nombre nuevo cada vez para que Coil no muestre la anterior
    override suspend fun guardar(clave: String, uri: String) = withContext(Dispatchers.IO) {
        val dir = carpeta ?: return@withContext
        val cr = resolver ?: return@withContext
        borrarArchivos(dir, clave)
        val archivo = File(dir, "${clave}_${System.currentTimeMillis()}.jpg")
        cr.openInputStream(Uri.parse(uri))?.use { entrada ->
            archivo.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        if (archivo.exists() && archivo.length() > 0) {
            _imagenes.update { it + (clave to ImagenGuardada.Propia(archivo.absolutePath)) }
        }
    }

    // deja el producto / museo sin imagen (también oculta la ilustración que trae la app)
    override suspend fun quitar(clave: String) = withContext(Dispatchers.IO) {
        val dir = carpeta ?: return@withContext
        borrarArchivos(dir, clave)
        File(dir, "$clave.quitada").createNewFile()
        _imagenes.update { it + (clave to ImagenGuardada.Quitada) }
    }

    // cuando se elimina el producto o la ficha, se borra todo lo suyo
    override suspend fun olvidar(clave: String) = withContext(Dispatchers.IO) {
        carpeta?.let { borrarArchivos(it, clave) }
        _imagenes.update { it - clave }
    }

    private fun borrarArchivos(dir: File, clave: String) {
        dir.listFiles()
            ?.filter { it.name == "$clave.quitada" || (it.name.startsWith("${clave}_") && it.name.endsWith(".jpg")) }
            ?.forEach { it.delete() }
    }
}
