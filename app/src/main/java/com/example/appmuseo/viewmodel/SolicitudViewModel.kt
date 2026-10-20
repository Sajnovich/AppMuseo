package com.example.appmuseo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.data.local.EstadoSolicitud
import com.example.appmuseo.data.local.SolicitudEntity
import com.example.appmuseo.data.local.codigoSolicitud
import com.example.appmuseo.model.Cotizacion
import com.example.appmuseo.model.Curso
import com.example.appmuseo.model.ItemCarrito
import com.example.appmuseo.model.SolicitudUiState
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.model.Zona
import com.example.appmuseo.repository.SolicitudRepository
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.Notificaciones
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = SolicitudRepository(
        AppDatabase.obtenerBaseDeDatos(application).solicitudDao()
    )
    private val sesionDataStore = SesionDataStore(application)

    private val _formulario = MutableStateFlow(SolicitudUiState())
    val formulario: StateFlow<SolicitudUiState> = _formulario

    // true mientras se guarda en room
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _solicitudes = MutableStateFlow<List<SolicitudEntity>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudEntity>> = _solicitudes

    private val _textoNotificacion = MutableStateFlow("")
    val textoNotificacion: StateFlow<String> = _textoNotificacion
    private val _mostrarNotificacion = MutableStateFlow(false)
    val mostrarNotificacion: StateFlow<Boolean> = _mostrarNotificacion

    // correo del usuario con sesion: dueño de las solicitudes nuevas
    private var usuarioActual = ""
    private var trabajoHistorial: Job? = null

    // se llama cada vez que cambia la sesion, asi cada perfil ve solo su historial
    fun cargarHistorial(correo: String) {
        if (correo == usuarioActual && trabajoHistorial != null) return
        usuarioActual = correo
        trabajoHistorial?.cancel()
        _solicitudes.value = emptyList()
        if (correo.isBlank()) return
        trabajoHistorial = viewModelScope.launch {
            repositorio.obtenerPorUsuario(correo).collect { lista -> _solicitudes.value = lista }
        }
    }

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

    fun onZonaChange(zona: Zona, pesoKg: Double, subtotal: Int) {
        try {
            val cotizacion = CotizadorEnvio.cotizar(zona, pesoKg, subtotal)
            _formulario.update {
                it.copy(zona = zona, cotizacion = cotizacion, errores = it.errores.copy(zona = null))
            }
        } catch (e: IllegalArgumentException) {
            _formulario.update {
                it.copy(zona = null, cotizacion = null, errores = it.errores.copy(zona = e.message))
            }
        }
    }

    // el envio ya se eligio en el carrito, aqui solo se copia
    fun usarEnvio(cotizacion: Cotizacion?) {
        _formulario.update {
            it.copy(
                zona = cotizacion?.zona,
                tipoDestino = cotizacion?.zona?.tipo ?: it.tipoDestino,
                cotizacion = cotizacion,
                errores = it.errores.copy(zona = null)
            )
        }
    }

    fun actualizarCotizacion(pesoKg: Double, subtotal: Int) {
        val zona = _formulario.value.zona ?: return
        if (pesoKg <= 0.0) return
        onZonaChange(zona, pesoKg, subtotal)
    }

    fun validarFormulario(requiereEnvio: Boolean): Boolean {
        val errores = Validaciones.validarSolicitud(_formulario.value, requiereEnvio)
        _formulario.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    fun confirmarSolicitud(items: List<ItemCarrito>, onExito: () -> Unit) {
        if (items.isEmpty() || _guardando.value) return
        val requiereEnvio = items.any { it.producto.requiereEnvio }
        if (!validarFormulario(requiereEnvio)) return

        val form = _formulario.value
        val subtotal = items.sumOf { it.subtotal }
        val costoEnvio = if (requiereEnvio) form.cotizacion?.costoEnvio ?: 0 else 0

        val hayCursos = items.any { it.producto is Curso }   // "is" pregunta por la subclase
        val tipo = when {
            hayCursos && !requiereEnvio -> "INSCRIPCIÓN"
            hayCursos && requiereEnvio -> "COMPRA + INSCRIPCIÓN"
            else -> "COMPRA"
        }

        val solicitud = SolicitudEntity(
            correoUsuario = usuarioActual,
            tipo = tipo,
            cantidadProductos = items.sumOf { it.cantidad },
            detalle = items.joinToString(", ") { "${it.cantidad}x ${it.producto.nombre}" },
            nombreCliente = form.nombre.trim(),
            correoCliente = form.correo.trim(),
            zonaEnvio = if (requiereEnvio) "${form.zona?.nombre ?: ""} · ${form.cotizacion?.tipoEnvio?.etiqueta ?: ""}" else "",
            direccion = if (requiereEnvio) form.direccion.trim() else "",
            subtotal = subtotal,
            costoEnvio = costoEnvio,
            total = subtotal + costoEnvio
        )

        _guardando.value = true
        viewModelScope.launch {
            val idNuevo = repositorio.insertar(solicitud)
            _formulario.value = SolicitudUiState()
            _guardando.value = false
            onExito()
            notificar(idNuevo.toInt(), "Solicitud registrada", "Solicitud ${codigoSolicitud(idNuevo.toInt())} registrada. Estado: pendiente.")
        }
    }

    // simula el avance del pedido. Devuelve el estado nuevo (null si ya estaba entregado)
    fun avanzarEstado(solicitud: SolicitudEntity): String? {
        val siguiente = EstadoSolicitud.siguiente(solicitud.estado) ?: return null
        viewModelScope.launch {
            repositorio.actualizarEstado(solicitud.id, siguiente)
            notificar(
                solicitud.id,
                "Pedido ${EstadoSolicitud.etiqueta(siguiente).lowercase()}",
                "Tu pedido ${codigoSolicitud(solicitud.id)} ahora está: ${EstadoSolicitud.etiqueta(siguiente)}."
            )
        }
        return siguiente
    }

    fun eliminar(solicitud: SolicitudEntity) {
        viewModelScope.launch { repositorio.eliminar(solicitud) }
    }

    private suspend fun notificar(id: Int, titulo: String, texto: String) {
        if (!sesionDataStore.obtenerNotificaciones().first()) return
        Notificaciones.mostrar(getApplication<Application>(), id, titulo, texto)
        _textoNotificacion.value = texto
        _mostrarNotificacion.value = true
        delay(3_500)
        _mostrarNotificacion.value = false
    }
}
