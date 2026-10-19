package com.example.appmuseo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appmuseo.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declaramos el DataStore UNA sola vez para toda la app
val Context.dataStore by preferencesDataStore(name = "preferencias_museo")

class SesionDataStore(private val context: Context) {

    private val SESION_ACTIVA = booleanPreferencesKey("sesion_activa")
    private val NOMBRE = stringPreferencesKey("nombre_usuario")
    private val CORREO = stringPreferencesKey("correo_usuario")
    private val TIPO = stringPreferencesKey("tipo_usuario")
    private val NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")

    suspend fun guardarSesion(perfil: PerfilUsuario) {
        context.dataStore.edit { preferencias ->
            preferencias[SESION_ACTIVA] = true
            preferencias[NOMBRE] = perfil.nombre
            preferencias[CORREO] = perfil.correo
            preferencias[TIPO] = perfil.tipoUsuario
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias[SESION_ACTIVA] = false
            preferencias.remove(NOMBRE)
            preferencias.remove(CORREO)
            preferencias.remove(TIPO)
        }
    }

    fun obtenerSesionActiva(): Flow<Boolean?> =
        context.dataStore.data.map { preferencias -> preferencias[SESION_ACTIVA] }

    fun obtenerUsuario(): Flow<PerfilUsuario> =
        context.dataStore.data.map { preferencias ->
            PerfilUsuario(
                nombre = preferencias[NOMBRE] ?: "",
                correo = preferencias[CORREO] ?: "",
                tipoUsuario = preferencias[TIPO] ?: ""
            )
        }

    suspend fun guardarNotificaciones(activas: Boolean) {
        context.dataStore.edit { preferencias -> preferencias[NOTIFICACIONES] = activas }
    }

    fun obtenerNotificaciones(): Flow<Boolean> =
        context.dataStore.data.map { preferencias -> preferencias[NOTIFICACIONES] ?: true }
}
