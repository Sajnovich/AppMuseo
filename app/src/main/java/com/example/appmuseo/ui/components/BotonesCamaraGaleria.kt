package com.example.appmuseo.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appmuseo.utils.crearUriParaFoto

@Composable
fun BotonesCamaraGaleria(
    onImagenObtenida: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    mostrarGaleria: Boolean = true,
    textoCamara: String = "Cámara"
) {
    val context = LocalContext.current
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }

    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) onImagenObtenida(uri)
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito: Boolean ->
        val uri = uriTemporal
        if (exito && uri != null) onImagenObtenida(uri)
    }

    fun abrirCamara() {
        try {
            val nuevaUri = crearUriParaFoto(context)
            uriTemporal = nuevaUri
            lanzadorCamara.launch(nuevaUri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        if (mostrarGaleria) {
            OutlinedButton(onClick = {
                try {
                    lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No se encontró una galería de imágenes", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }
        Button(onClick = { abrirCamara() }) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(textoCamara)
        }
    }
}
