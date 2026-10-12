package com.example.appmuseo.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.core.content.ContextCompat
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

    // 1) GALERÍA: GetContent abre el selector de imágenes. Devuelve null si el usuario cancela.
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

    // 3) PERMISO: la cámara es un permiso "peligroso", hay que pedirlo mientras la app se usa.
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (concedido) {
            abrirCamara()
        } else {
            Toast.makeText(context, "Sin permiso de cámara no se puede tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        if (mostrarGaleria) {
            OutlinedButton(onClick = {
                try {
                    lanzadorGaleria.launch("image/*")
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No se encontró una galería de imágenes", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }
        Button(onClick = {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (tienePermiso) abrirCamara() else lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(textoCamara)
        }
    }
}
