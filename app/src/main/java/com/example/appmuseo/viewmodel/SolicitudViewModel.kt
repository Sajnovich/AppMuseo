package com.example.appmuseo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.SolicitudUiState
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import com.example.appmuseo.repository.SolicitudRepository
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SOLICITUDES de compra / inscripción:
 *  - Formulario con validaciones (Guía 11)
 *  - Cotización del envío según la zona
 *  - Persistencia en ROOM (lista de solicitudes con estado pendiente / completada)
 *  - Notificación "push" simulada (mensaje animado dentro de la app)
 */
class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = SolicitudRepository(
        AppDatabase.obtenerBaseDeDatos(application).solicitudDao()
    )
    private val sesionDataStore = SesionDataStore(application)

    private val _formulario = MutableStateFlow(SolicitudUiState())
    val formulario: StateFlow<SolicitudUiState> = _formulario

    // true mientras se guarda en Room: evita registrar dos veces la misma solicitud con un doble toque
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _solicitudes = MutableStateFlow<List<SolicitudEntity>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudEntity>> = _solicitudes

    // Notificación simulada: texto + visible (mismo patrón que mostrarMensaje en la Guía 12)
    private val _textoNotificacion = MutableStateFlow("")
    val textoNotificacion: StateFlow<String> = _textoNotificacion
    private val _mostrarNotificacion = MutableStateFlow(false)
    val mostrarNotificacion: StateFlow<Boolean> = _mostrarNotificacion

    init {
        // Room entrega un Flow: cada vez que cambia la tabla recibimos la lista nueva
        viewModelScope.launch {
            repositorio.obtenerTodas().collect { lista -> _solicitudes.value = lista }
        }
    }

    // ── Formulario ────────────────────────────────────────────────────────────

    /** Completa nombre y correo con los datos de la sesión (si el usuario aún no escribió). */
    fun precargarDatos(nombre: String, correo: String) {
        _formulario.update {
            it.copy(
                nombre = it.nombre.ifBlank { nombre },
                correo = it.correo.ifBlank { correo }
            )
        }
    }

    fun onNombreChange(valor: String) {
        _formulario.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    }

    fun onCorreoChange(valor: String) {
        _formulario.update { it.copy(correo = valor, errores = it.errores.copy(correo = null)) }
    }

    fun onDireccionChange(valor: String) {
        _formulario.update { it.copy(direccion = valor, errores = it.errores.copy(direccion = null)) }
    }

    fun onAceptaTerminosChange(valor: Boolean) {
        _formulario.update { it.copy(aceptaTerminos = valor, errores = it.errores.copy(terminos = null)) }
    }

    fun onTipoDestinoChange(tipo: TipoDestino) {
        _formulario.update { it.copy(tipoDestino = tipo, zona = null, cotizacion = null) }
    }

    /** Al elegir la zona se cotiza el envío con el peso y el subtotal del carrito. */
    fun onZonaChange(zona: Zona, pesoKg: Double, subtotal: Int) {
        try {
            val cotizacion = CotizadorEnvio.cotizar(zona, pesoKg, subtotal)
            _formulario.update {
                it.copy(zona = zona, cotizacion = cotizacion, errores = it.errores.copy(zona = null))
            }
        } catch (e: IllegalArgumentException) {
            // Ej: el pedido pesa más de 30 kg
            _formulario.update {
                it.copy(zona = null, cotizacion = null, errores = it.errores.copy(zona = e.message))
            }
        }
    }

    /**
     * Si el carrito cambió después de elegir la zona (más productos, otro peso o monto),
     * se vuelve a cotizar para que el costo de envío no quede desactualizado.
     */
    fun actualizarCotizacion(pesoKg: Double, subtotal: Int) {
        val zona = _formulario.value.zona ?: return
        if (pesoKg <= 0.0) return   // solo cursos: no hay envío que cotizar
        onZonaChange(zona, pesoKg, subtotal)
    }

    fun validarFormulario(requiereEnvio: Boolean): Boolean {
        val errores = Validaciones.validarSolicitud(_formulario.value, requiereEnvio)
        _formulario.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    // ── Room ──────────────────────────────────────────────────────────────────

    /**
     * Guarda la solicitud en Room. onExito se ejecuta después de guardar
     * (la pantalla lo usa para vaciar el carrito y navegar).
     */
    fun confirmarSolicitud(items: List<ItemCarrito>, onExito: () -> Unit) {
        if (items.isEmpty() || _guardando.value) return
        val requiereEnvio = items.any { it.producto.requiereEnvio }
        if (!validarFormulario(requiereEnvio)) return

        val form = _formulario.value
        val subtotal = items.sumOf { it.subtotal }
        val costoEnvio = if (requiereEnvio) form.cotizacion?.costoEnvio ?: 0 else 0

        val hayCursos = items.any { it.producto.categoria == Categoria.CURSO }
        val tipo = when {
            hayCursos && !requiereEnvio -> "INSCRIPCIÓN"
            hayCursos && requiereEnvio -> "COMPRA + INSCRIPCIÓN"
            else -> "COMPRA"
        }

        val solicitud = SolicitudEntity(
            tipo = tipo,
            detalle = items.joinToString(", ") { "${it.cantidad}x ${it.producto.nombre}" },
            nombreCliente = form.nombre.trim(),
            correoCliente = form.correo.trim(),
            zonaEnvio = if (requiereEnvio) form.zona?.nombre ?: "" else "",
            direccion = if (requiereEnvio) form.direccion.trim() else "",
            subtotal = subtotal,
            costoEnvio = costoEnvio,
            total = subtotal + costoEnvio
        )

        _guardando.value = true
        viewModelScope.launch {
            val idNuevo = repositorio.insertar(solicitud)
            _formulario.value = SolicitudUiState()   // limpia el formulario
            _guardando.value = false
            onExito()
            notificar("Solicitud #$idNuevo registrada. Estado: pendiente.")
        }
    }

    /** Simula el avance del proceso: pendiente → completada. */
    fun marcarCompletada(solicitud: SolicitudEntity) {
        viewModelScope.launch {
            repositorio.actualizarEstado(solicitud.id, EstadoSolicitud.COMPLETADA)
            notificar("Solicitud #${solicitud.id} completada. ¡Gracias por tu compra!")
        }
    }

    fun eliminar(solicitud: SolicitudEntity) {
        viewModelScope.launch { repositorio.eliminar(solicitud) }
    }

    /** "Push" simulado: solo se muestra si el usuario tiene activadas las notificaciones. */
    private suspend fun notificar(texto: String) {
        if (!sesionDataStore.obtenerNotificaciones().first()) return
        _textoNotificacion.value = texto
        _mostrarNotificacion.value = true
        delay(3_500)
        _mostrarNotificacion.value = false
    }
}
