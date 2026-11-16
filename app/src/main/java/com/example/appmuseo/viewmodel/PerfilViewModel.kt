package com.example.appmuseo.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuseo.data.SesionDataStore
import com.example.appmuseo.data.local.AppDatabase
import com.example.appmuseo.model.EditarPerfilUiState
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.ResultadoActualizacion
import com.example.appmuseo.data.remote.RetrofitInstance
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.repository.SolicitudRepository
import com.example.appmuseo.utils.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val sesionDataStore = SesionDataStore(application)
    private val baseDeDatos = AppDatabase.obtenerBaseDeDatos(application)
    private val authRepository = AuthRepository(demoraMs = 600, usuarioDao = baseDeDatos.usuarioDao(), api = RetrofitInstance.usuarios)
    private val solicitudRepository = SolicitudRepository(baseDeDatos.solicitudDao())

    private val _imagenUri = MutableStateFlow<Uri?>(null)
    val imagenUri: StateFlow<Uri?> = _imagenUri

    private val _notificacionesActivas = MutableStateFlow<Boolean?>(null)
    val notificacionesActivas: StateFlow<Boolean?> = _notificacionesActivas

    private val _mostrarMensaje = MutableStateFlow(false)
    val mostrarMensaje: StateFlow<Boolean> = _mostrarMensaje

    private val _edicion = MutableStateFlow(EditarPerfilUiState())
    val edicion: StateFlow<EditarPerfilUiState> = _edicion

    init {
        viewModelScope.launch {
            _notificacionesActivas.value = sesionDataStore.obtenerNotificaciones().first()
        }
    }

    // ---------- Foto de perfil ----------

    // al iniciar sesion se busca la foto guardada de ese usuario
    fun cargarFoto(correo: String) {
        viewModelScope.launch {
            val ruta = if (correo.isBlank()) null else sesionDataStore.obtenerFoto(correo).first()
            val archivo = ruta?.let { File(it) }
            _imagenUri.value = if (archivo != null && archivo.exists()) Uri.fromFile(archivo) else null
        }
    }

    // La foto de la camara queda en cache (se puede borrar sola).
    // Por eso se copia a la carpeta privada de la app y se guarda la ruta en DataStore
    fun guardarFoto(uri: Uri, correo: String) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val destino = withContext(Dispatchers.IO) {
                val carpeta = File(app.filesDir, "fotos").apply { mkdirs() }
                // nombre nuevo cada vez para que Coil no muestre la foto anterior
                carpeta.listFiles()?.filter { it.name.startsWith(prefijoFoto(correo)) }?.forEach { it.delete() }
                val archivo = File(carpeta, "${prefijoFoto(correo)}${System.currentTimeMillis()}.jpg")
                app.contentResolver.openInputStream(uri)?.use { entrada ->
                    archivo.outputStream().use { salida -> entrada.copyTo(salida) }
                }
                archivo
            }
            if (destino.exists() && destino.length() > 0) {
                sesionDataStore.guardarFoto(correo, destino.absolutePath)
                _imagenUri.value = Uri.fromFile(destino)
            }
        }
    }

    private fun prefijoFoto(correo: String) = "perfil_${correo.lowercase().hashCode()}_"

    fun alternarNotificaciones() {
        viewModelScope.launch {
            val nuevoValor = !(_notificacionesActivas.value ?: true)
            sesionDataStore.guardarNotificaciones(nuevoValor)
            _notificacionesActivas.value = nuevoValor

            _mostrarMensaje.value = true   // mensaje animado
            delay(2_000)
            _mostrarMensaje.value = false
        }
    }

    // ---------- Editar perfil ----------

    fun iniciarEdicion(perfil: PerfilUsuario) {
        _edicion.value = EditarPerfilUiState(nombre = perfil.nombre, correo = perfil.correo)
    }

    fun onNombreChange(valor: String) = _edicion.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    fun onCorreoChange(valor: String) = _edicion.update { it.copy(correo = valor, errores = it.errores.copy(correo = null)) }
    fun onClaveActualChange(valor: String) = _edicion.update { it.copy(claveActual = valor, errores = it.errores.copy(claveActual = null)) }
    fun onClaveNuevaChange(valor: String) = _edicion.update { it.copy(claveNueva = valor, errores = it.errores.copy(claveNueva = null)) }
    fun onRepetirClaveChange(valor: String) = _edicion.update { it.copy(repetirClave = valor, errores = it.errores.copy(repetirClave = null)) }
    fun onCambiarClaveChange(valor: Boolean) = _edicion.update {
        it.copy(cambiarClave = valor, claveNueva = "", repetirClave = "", errores = it.errores.copy(claveNueva = null, repetirClave = null))
    }

    fun guardarCambios(correoActual: String, onExito: (PerfilUsuario) -> Unit) {
        val estado = _edicion.value
        if (estado.guardando) return
        val errores = Validaciones.validarEdicionPerfil(estado)
        _edicion.update { it.copy(errores = errores, errorGeneral = null) }
        if (errores.hayErrores()) return

        _edicion.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val resultado = authRepository.actualizarPerfil(
                correoActual = correoActual,
                nombre = estado.nombre,
                nuevoCorreo = estado.correo,
                claveActual = estado.claveActual,
                nuevaClave = if (estado.cambiarClave) estado.claveNueva else null
            )
            when (resultado) {
                is ResultadoActualizacion.Exito -> {
                    val nuevoCorreo = resultado.perfil.correo
                    if (nuevoCorreo != correoActual.lowercase()) {
                        // el historial y la foto se mueven al correo nuevo
                        solicitudRepository.cambiarDueno(correoActual.lowercase(), nuevoCorreo)
                        val foto = sesionDataStore.obtenerFoto(correoActual).first()
                        sesionDataStore.guardarFoto(nuevoCorreo, foto)
                        sesionDataStore.guardarFoto(correoActual, null)
                    }
                    _edicion.update { it.copy(guardando = false, guardado = true, claveActual = "", claveNueva = "", repetirClave = "") }
                    onExito(resultado.perfil)
                }
                is ResultadoActualizacion.Error -> _edicion.update {
                    when (resultado.campo) {
                        "claveActual" -> it.copy(guardando = false, errores = it.errores.copy(claveActual = resultado.mensaje))
                        "correo" -> it.copy(guardando = false, errores = it.errores.copy(correo = resultado.mensaje))
                        else -> it.copy(guardando = false, errorGeneral = resultado.mensaje)
                    }
                }
            }
        }
    }
}
