package com.example.appmuseo.utils

import java.security.MessageDigest

object Seguridad {

    fun hashSha256(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8))
        // cada byte se escribe como 2 digitos hexadecimales
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}
