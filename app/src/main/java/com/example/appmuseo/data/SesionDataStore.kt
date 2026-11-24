package com.example.appmuseo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appmuseo.model.PerfilUsuario
import com.example.appmuseo.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declaramos el DataStore UNA sola vez para toda la app
val Context.dataStore by preferencesDataStore(name = "preferencias_museo")

class SesionDataStore(private val context: Context) {

    private val SESION_ACTIVA = booleanPreferencesKey("sesion_activa")
    private val NOMBRE = stringPreferencesKey("nombre_usuario")
    private val CORREO = stringPreferencesKey("correo_usuario")
    private val TIPO = stringPreferencesKey("tipo_usuario")
    private val ROL = stringPreferencesKey("rol_usuario")
    private val AVATAR = intPreferencesKey("avatar_usuario")
    private val NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")
    private val TOKEN = stringPreferencesKey("token_jwt")
    private val TOKEN_EXPIRA = stringPreferencesKey("token_expira")

    // token JWT de la sesión (para no tener que iniciar sesión cada vez que se abre la app)
    suspend fun guardarToken(token: String?, expiraEn: String?) {
        context.dataStore.edit { preferencias ->
            if (token == null) preferencias.remove(TOKEN) else preferencias[TOKEN] = token
            if (expiraEn == null) preferencias.remove(TOKEN_EXPIRA) else preferencias[TOKEN_EXPIRA] = expiraEn
        }
    }

    fun obtenerToken(): Flow<Pair<String?, String?>> =
        context.dataStore.data.map { preferencias -> preferencias[TOKEN] to preferencias[TOKEN_EXPIRA] }

    suspend fun guardarSesion(perfil: PerfilUsuario) {
        context.dataStore.edit { preferencias ->
            preferencias[SESION_ACTIVA] = true
            preferencias[NOMBRE] = perfil.nombre
            preferencias[CORREO] = perfil.correo
            preferencias[TIPO] = perfil.tipoUsuario
            preferencias[ROL] = perfil.rol.name
            preferencias[AVATAR] = perfil.avatar
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias[SESION_ACTIVA] = false
            preferencias.remove(NOMBRE)
            preferencias.remove(CORREO)
            preferencias.remove(TIPO)
            preferencias.remove(ROL)
            preferencias.remove(AVATAR)
            preferencias.remove(TOKEN)
            preferencias.remove(TOKEN_EXPIRA)
        }
    }

    fun obtenerSesionActiva(): Flow<Boolean?> =
        context.dataStore.data.map { preferencias -> preferencias[SESION_ACTIVA] }

    fun obtenerUsuario(): Flow<PerfilUsuario> =
        context.dataStore.data.map { preferencias ->
            PerfilUsuario(
                nombre = preferencias[NOMBRE] ?: "",
                correo = preferencias[CORREO] ?: "",
                tipoUsuario = preferencias[TIPO] ?: "",
                rol = Rol.entries.find { it.name == preferencias[ROL] } ?: Rol.CLIENTE,
                avatar = preferencias[AVATAR] ?: 0
            )
        }

    // la foto se guarda por usuario: cada cuenta tiene la suya
    private fun claveFoto(correo: String) = stringPreferencesKey("foto_" + correo.lowercase())

    suspend fun guardarFoto(correo: String, ruta: String?) {
        context.dataStore.edit { preferencias ->
            if (ruta == null) preferencias.remove(claveFoto(correo)) else preferencias[claveFoto(correo)] = ruta
        }
    }

    fun obtenerFoto(correo: String): Flow<String?> =
        context.dataStore.data.map { preferencias -> preferencias[claveFoto(correo)] }

    suspend fun guardarNotificaciones(activas: Boolean) {
        context.dataStore.edit { preferencias -> preferencias[NOTIFICACIONES] = activas }
    }

    fun obtenerNotificaciones(): Flow<Boolean> =
        context.dataStore.data.map { preferencias -> preferencias[NOTIFICACIONES] ?: true }
}
