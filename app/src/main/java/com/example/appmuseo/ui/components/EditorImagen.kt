package com.example.appmuseo.ui.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appmuseo.model.CambioImagen
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBotonContenedor
import com.example.appmuseo.utils.ImagenGuardada
import com.example.appmuseo.utils.ImagenesCatalogo
import com.example.appmuseo.utils.tieneImagen
import java.io.File

// Muestra la imagen de un producto o museo:
//  1) la foto que dejó el Administrador / Investigador (cámara o galería)
//  2) si no hay, la ilustración que trae la app (porDefecto)
//  3) si se quitó o no tiene, el respaldo (ícono o degradado)
@Composable
fun ImagenCatalogo(
    clave: String,
    @DrawableRes porDefecto: Int,
    descripcion: String,
    modifier: Modifier = Modifier,
    respaldo: @Composable (Modifier) -> Unit
) {
    val imagenes by ImagenesCatalogo.imagenes.collectAsState()
    when (val guardada = imagenes[clave]) {
        is ImagenGuardada.Propia -> AsyncImage(
            model = File(guardada.ruta),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        ImagenGuardada.Quitada -> respaldo(modifier)
        null -> if (porDefecto != 0) {
            Image(painterResource(porDefecto), contentDescription = descripcion, contentScale = ContentScale.Crop, modifier = modifier)
        } else respaldo(modifier)
    }
}

// Sección "Imagen" de los formularios de gestión: vista previa + Galería / Cámara + Quitar.
// Nada se guarda hasta tocar "Guardar cambios"; mientras tanto se puede deshacer.
@Composable
fun EditorImagen(
    clave: String?,                 // null = producto o museo nuevo (aún no tiene id)
    @DrawableRes porDefecto: Int,
    cambio: CambioImagen,
    onCambio: (CambioImagen) -> Unit
) {
    val imagenes by ImagenesCatalogo.imagenes.collectAsState()
    val tieneActual = clave != null && tieneImagen(imagenes[clave], traePorDefecto = porDefecto != 0)
    val hayImagen = cambio is CambioImagen.Nueva || (cambio == CambioImagen.SinCambios && tieneActual)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        Text("Imagen", style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                cambio is CambioImagen.Nueva -> AsyncImage(
                    model = Uri.parse(cambio.uri),
                    contentDescription = "Imagen nueva",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                hayImagen && clave != null -> ImagenCatalogo(clave, porDefecto, "Imagen actual", Modifier.fillMaxSize()) { SinImagen(it) }
                else -> SinImagen(Modifier.fillMaxSize())
            }
            if (cambio != CambioImagen.SinCambios) {
                Text(
                    if (cambio == CambioImagen.Quitar) "Se quitará al guardar" else "Se aplicará al guardar",
                    style = MaterialTheme.typography.labelSmall,
                    color = AzulNoche,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(DoradoBotonContenedor, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // reutiliza los mismos botones de la foto de perfil
        BotonesCamaraGaleria(
            onImagenObtenida = { uri -> onCambio(CambioImagen.Nueva(uri.toString())) },
            modifier = Modifier.fillMaxWidth(),
            textoCamara = "Tomar foto"
        )

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            if (hayImagen) {
                TextButton(onClick = { onCambio(CambioImagen.Quitar) }) {
                    Icon(Icons.Filled.HideImage, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp))
                    Text("Quitar imagen", color = MaterialTheme.colorScheme.error)
                }
            }
            if (cambio != CambioImagen.SinCambios) {
                TextButton(onClick = { onCambio(CambioImagen.SinCambios) }) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Deshacer")
                }
            }
        }
    }
}

@Composable
private fun SinImagen(modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
        Text("Sin imagen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// fondo de color con ícono, para listas cuando no hay imagen
@Composable
fun RespaldoIcono(modifier: Modifier, icono: androidx.compose.ui.graphics.vector.ImageVector, fondo: Color = DoradoBotonContenedor) {
    Box(modifier = modifier.background(fondo), contentAlignment = Alignment.Center) {
        Icon(icono, contentDescription = null, tint = AzulNoche)
    }
}
