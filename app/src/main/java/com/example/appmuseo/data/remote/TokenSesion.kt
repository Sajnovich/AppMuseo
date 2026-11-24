package com.example.appmuseo.data.remote

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Token JWT de la sesión activa. Lo entrega ms-usuarios al iniciar sesión y el interceptor de OkHttp
// (RetrofitInstance) lo agrega en cada petición como "Authorization: Bearer <token>".
// Se guarda también en DataStore para que la sesión siga al reabrir la app.
object TokenSesion {

    @Volatile
    var token: String? = null
        private set

    // fecha de vencimiento tal como la manda el backend (ej: 2026-11-24T22:30:00)
    @Volatile
    var expiraEn: String? = null
        private set

    fun guardar(token: String?, expiraEn: String?) {
        this.token = token
        this.expiraEn = expiraEn
    }

    fun limpiar() = guardar(null, null)

    fun hayToken(): Boolean = !token.isNullOrBlank()

    // para mostrarlo en pantalla sin exponer el token completo
    fun abreviado(): String? = token?.let { if (it.length > 26) it.take(16) + "…" + it.takeLast(8) else it }

    // "22:30" a partir de la fecha de expiración (null si no se puede leer)
    fun horaExpiracion(): String? = try {
        expiraEn?.let { LocalDateTime.parse(it.take(19)).format(DateTimeFormatter.ofPattern("HH:mm")) }
    } catch (e: Exception) {
        null
    }
}
