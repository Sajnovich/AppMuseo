package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.remote.ResultadoApi
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.model.CambioImagen
import com.example.appmuseo.model.FormMuseo
import com.example.appmuseo.model.FormProducto
import com.example.appmuseo.model.GestionUiState
import com.example.appmuseo.repository.GestionRepository
import com.example.appmuseo.utils.AlmacenImagenes
import com.example.appmuseo.utils.ImagenesCatalogo
import com.example.appmuseo.utils.claveMuseo
import com.example.appmuseo.utils.claveProducto
import com.example.appmuseo.utils.ValidacionesGestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de las funciones de gestión:
//  - Administrador: alta, modificación y eliminación de productos/cursos y cambio de estado de las solicitudes
//  - Investigador: registro y corrección de fichas de museos
// Cada acción llama al backend con el token JWT; si el rol no corresponde, el backend responde 403.
class GestionViewModel(
    private val repositorio: GestionRepository = GestionRepository(),
    private val imagenes: AlmacenImagenes = ImagenesCatalogo
) : ViewModel() {

    private val _estado = MutableStateFlow(GestionUiState())
    val estado: StateFlow<GestionUiState> = _estado

    // ---------- productos y cursos ----------

    fun cargarProductos() = cargar { repositorio.productos().alExito { lista -> _estado.update { it.copy(productos = lista) } } }

    fun nuevoProducto() = _estado.update { it.copy(formProducto = FormProducto(), errores = emptyMap(), mensaje = null) }

    fun editarProducto(producto: ProductoDto) =
        _estado.update { it.copy(formProducto = FormProducto.desde(producto), errores = emptyMap(), mensaje = null) }

    // cada campo del formulario se actualiza con copy, y se borra el error de ese campo
    fun onProductoChange(campo: String, cambio: (FormProducto) -> FormProducto) =
        _estado.update { it.copy(formProducto = cambio(it.formProducto), errores = it.errores - campo) }

    fun guardarProducto(onGuardado: () -> Unit = {}) {
        val form = _estado.value.formProducto
        val errores = ValidacionesGestion.validarProducto(form)
        if (errores.isNotEmpty()) {
            _estado.update { it.copy(errores = errores) }
            return
        }
        guardar {
            when (val r = repositorio.guardarProducto(form.id, form.aRequest())) {
                is ResultadoApi.Exito -> {
                    val texto = if (form.esNuevo) "«${r.datos.nombre}» se agregó al catálogo" else "«${r.datos.nombre}» se actualizó"
                    _estado.update { e -> e.copy(productos = reemplazar(e.productos, r.datos) { it.id }, mensaje = texto) }
                    onGuardado()
                    // la imagen se aplica después de guardar: así un producto nuevo ya tiene su id
                    aplicarImagen(claveProducto(r.datos.id), form.imagen)
                }
                else -> manejarError(r)
            }
        }
    }

    fun eliminarProducto(producto: ProductoDto) = guardar {
        when (val r = repositorio.eliminarProducto(producto.id)) {
            is ResultadoApi.Exito -> {
                _estado.update { e ->
                    e.copy(productos = e.productos.filterNot { it.id == producto.id }, mensaje = "«${producto.nombre}» se eliminó del catálogo")
                }
                imagenes.olvidar(claveProducto(producto.id))
            }
            else -> manejarError(r)
        }
    }

    // ---------- fichas de museos ----------

    fun cargarMuseos() = cargar { repositorio.museos().alExito { lista -> _estado.update { it.copy(museos = lista) } } }

    fun nuevoMuseo() = _estado.update { it.copy(formMuseo = FormMuseo(), errores = emptyMap(), mensaje = null) }

    fun editarMuseo(museo: MuseoDto) =
        _estado.update { it.copy(formMuseo = FormMuseo.desde(museo), errores = emptyMap(), mensaje = null) }

    fun onMuseoChange(campo: String, cambio: (FormMuseo) -> FormMuseo) =
        _estado.update { it.copy(formMuseo = cambio(it.formMuseo), errores = it.errores - campo) }

    fun guardarMuseo(onGuardado: () -> Unit = {}) {
        val form = _estado.value.formMuseo
        val errores = ValidacionesGestion.validarMuseo(form)
        if (errores.isNotEmpty()) {
            _estado.update { it.copy(errores = errores) }
            return
        }
        guardar {
            when (val r = repositorio.guardarMuseo(form.id, form.aRequest())) {
                is ResultadoApi.Exito -> {
                    val texto = if (form.esNuevo) "Ficha de «${r.datos.nombre}» registrada" else "Ficha de «${r.datos.nombre}» corregida"
                    _estado.update { e -> e.copy(museos = reemplazar(e.museos, r.datos) { it.id }, mensaje = texto) }
                    onGuardado()
                    aplicarImagen(claveMuseo(r.datos.id), form.imagen)
                }
                else -> manejarError(r)
            }
        }
    }

    fun eliminarMuseo(museo: MuseoDto) = guardar {
        when (val r = repositorio.eliminarMuseo(museo.id)) {
            is ResultadoApi.Exito -> {
                _estado.update { e ->
                    e.copy(museos = e.museos.filterNot { it.id == museo.id }, mensaje = "Se eliminó la ficha de «${museo.nombre}»")
                }
                imagenes.olvidar(claveMuseo(museo.id))
            }
            else -> manejarError(r)
        }
    }

    // ---------- solicitudes recibidas ----------

    fun cargarPedidos() = cargar { repositorio.pedidos().alExito { lista -> _estado.update { it.copy(pedidos = lista) } } }

    fun filtrarPedidos(estado: String?) = _estado.update { it.copy(filtroEstado = estado) }

    // Pendiente -> Confirmada -> Enviada -> Entregada (lo decide ms-pedidos)
    fun avanzarPedido(id: Long) = guardar {
        when (val r = repositorio.avanzarPedido(id)) {
            is ResultadoApi.Exito -> _estado.update { e ->
                e.copy(pedidos = reemplazar(e.pedidos, r.datos) { it.id }, mensaje = "${r.datos.codigo} ahora está ${r.datos.estado.lowercase()}")
            }
            else -> manejarError(r)
        }
    }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null) }

    // ---------- ayudas ----------

    // imagen elegida en el formulario: se guarda, se quita o se deja igual
    private suspend fun aplicarImagen(clave: String, cambio: CambioImagen) {
        when (cambio) {
            is CambioImagen.Nueva -> imagenes.guardar(clave, cambio.uri)
            CambioImagen.Quitar -> imagenes.quitar(clave)
            CambioImagen.SinCambios -> Unit
        }
    }

    private fun cargar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            accion()
            _estado.update { it.copy(cargando = false) }
        }
    }

    private fun guardar(accion: suspend () -> Unit) {
        if (_estado.value.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            accion()
            _estado.update { it.copy(guardando = false) }
        }
    }

    private suspend fun <T> ResultadoApi<T>.alExito(accion: (T) -> Unit) {
        if (this is ResultadoApi.Exito) accion(datos) else manejarError(this)
    }

    // traduce la respuesta del backend a un mensaje claro para la pantalla
    private fun manejarError(r: ResultadoApi<*>) {
        val mensaje = when (r) {
            is ResultadoApi.ErrorServidor -> when (r.codigo) {
                401 -> "Tu sesión venció o no tiene token. Cierra sesión y vuelve a entrar"
                403 -> "Tu rol no tiene permiso para esta acción"
                else -> r.mensaje
            }
            is ResultadoApi.SinConexion -> "Sin conexión con el servidor: no se guardaron los cambios"
            is ResultadoApi.Exito -> null
        }
        val campos = (r as? ResultadoApi.ErrorServidor)?.campos ?: emptyMap()
        _estado.update { it.copy(mensaje = mensaje, errores = it.errores + campos) }
    }

    // reemplaza el elemento con el mismo id, o lo agrega al principio si es nuevo
    private fun <T> reemplazar(lista: List<T>, nuevo: T, id: (T) -> Long): List<T> =
        if (lista.any { id(it) == id(nuevo) }) lista.map { if (id(it) == id(nuevo)) nuevo else it }
        else listOf(nuevo) + lista
}
