package com.example.appmuseo.utils

import java.security.MessageDigest

/**
 * Utilidades de seguridad. Un HASH transforma la contraseña en un texto irreconocible
 * de largo fijo (64 caracteres en SHA-256). Es de una sola vía: no se puede "deshacer".
 * Al iniciar sesión se calcula el hash de lo escrito y se compara con el guardado.
 * (En la Unidad 3 el backend debería usar además "sal" y un algoritmo como bcrypt.)
 */
object Seguridad {

    fun hashSha256(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8))
        // Cada byte se escribe como 2 dígitos hexadecimales: 0x0f → "0f"
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}
