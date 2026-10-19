package com.example.appmuseo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuseo.model.Notificacion
import com.example.appmuseo.model.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class NotificacionesViewModel : ViewModel() {

    // por ahora son de ejemplo, despues se llenan con las solicitudes
    private val _notificaciones = MutableStateFlow(
        listOf(
            Notificacion(1, "Nuevo curso disponible", "Conservación preventiva de colecciones abre inscripciones.", TipoNotificacion.CURSO, "Hace 2 h"),
            Notificacion(2, "Museo agregado al mapa", "El Museo Nacional de Colombia ya tiene su recorrido virtual.", TipoNotificacion.MUSEO, "Hace 1 día"),
            Notificacion(3, "Exposición destacada", "Patrimonio que nos une: textiles andinos.", TipoNotificacion.NOVEDAD, "Hace 3 días", leida = true)
        )
    )
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones

    fun cantidadNoLeidas(): Int = _notificaciones.value.count { !it.leida }

    fun marcarLeida(id: Int) {
        _notificaciones.update { lista -> lista.map { if (it.id == id) it.copy(leida = true) else it } }
    }

    fun marcarTodasLeidas() {
        _notificaciones.update { lista -> lista.map { it.copy(leida = true) } }
    }

    fun eliminar(id: Int) {
        _notificaciones.update { lista -> lista.filterNot { it.id == id } }
    }

    // la mas nueva queda arriba
    fun agregar(titulo: String, mensaje: String, tipo: TipoNotificacion) {
        _notificaciones.update { lista ->
            val nuevoId = (lista.maxOfOrNull { it.id } ?: 0) + 1
            listOf(Notificacion(nuevoId, titulo, mensaje, tipo, "Ahora")) + lista
        }
    }
}
