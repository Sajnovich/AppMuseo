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

/**
 * BOTONES DE CÁMARA Y GALERÍA (Guía 13) con la API moderna ActivityResult.
 * rememberLauncherForActivityResult registra un "lanzador" y recibe la respuesta en un callback.
 *
 * @param onImagenObtenida se llama con el Uri de la imagen SOLO si el usuario no canceló.
 */
@Composable
fun BotonesCamaraGaleria(
    onImagenObtenida: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    mostrarGaleria: Boolean = true,
    textoCamara: String = "Cámara"
) {
    val context = LocalContext.current
    // Uri donde la cámara guardará la foto (lo crea FileProvider antes de abrir la cámara).
    // rememberSaveable (no remember): si Android cierra y recrea la Activity mientras la cámara
    // está abierta, el Uri se recupera y la foto no se pierde.
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }

    // 1) GALERÍA: PickVisualMedia abre el selector de fotos del sistema (Android 13+, y una
    //    versión compatible en equipos anteriores). No necesita permisos. Devuelve null si se cancela.
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) onImagenObtenida(uri)        // si cancela, no hacemos nada → no crashea
    }

    // 2) CÁMARA: TakePicture guarda la foto en el Uri que le pasamos y devuelve true / false.
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
            // Control de errores: equipo sin app de cámara → avisamos en vez de cerrar la app
            Toast.makeText(context, "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // 3) Sin permiso CAMERA: la foto la toma la app de Cámara del teléfono (ver AndroidManifest.xml)

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
