package com.example.appmuseo.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Crea un archivo vacío en la caché de la app y devuelve un Uri "seguro" para él.
 * La app de cámara escribirá la foto en ese Uri (Guía 13: almacenamiento temporal con FileProvider).
 * La autoridad "<paquete>.fileprovider" debe ser igual a la declarada en AndroidManifest.xml.
 */
fun crearUriParaFoto(context: Context): Uri {
    val archivo = File.createTempFile("foto_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
