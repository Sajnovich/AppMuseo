package com.example.appmuseo.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriParaFoto(context: Context): Uri {
    val archivo = File.createTempFile("foto_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
