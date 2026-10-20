package com.example.appmuseo.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Museo
import com.example.appmuseo.ui.components.CabeceraMuseo
import com.example.appmuseo.ui.components.Estrellas
import com.example.appmuseo.ui.components.EtiquetaMuseo
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel

@Composable
fun DetalleMuseoScreen(
    museoId: Int,
    mapaViewModel: MapaViewModel,
    favoritosViewModel: FavoritosViewModel,
    mainViewModel: MainViewModel
) {
    val museo = mapaViewModel.buscarMuseo(museoId)
    val favoritos by favoritosViewModel.museos.collectAsState()
    val context = LocalContext.current

    if (museo == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Museo no encontrado")
            }
        }
        return
    }

    val esFavorito = museo.id in favoritos
    // el corazon "late" al guardarlo
    val escala by animateFloatAsState(
        targetValue = if (esFavorito) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "corazon"
    )
    val colorCorazon by animateColorAsState(if (esFavorito) Color(0xFFD32F2F) else Color.Gray, label = "colorCorazon")

    fun alternarFavorito() {
        val guardado = favoritosViewModel.alternarMuseo(museo.id)
        Toast.makeText(context, if (guardado) "Guardado en favoritos" else "Quitado de favoritos", Toast.LENGTH_SHORT).show()
    }

    fun abrir(uri: Uri, error: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraMuseo(
                museo = museo,
                tamanoIcono = 120.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                IconButton(
                    onClick = { mainViewModel.navigateBack() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            // tarjeta blanca que se monta sobre la cabecera
            Column(
                modifier = Modifier
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        museo.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { alternarFavorito() }) {
                        Icon(
                            if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = colorCorazon,
                            modifier = Modifier
                                .size(30.dp)
                                .graphicsLayer(scaleX = escala, scaleY = escala)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${museo.ciudad}, ${museo.pais}", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    museo.etiquetas().forEach { EtiquetaMuseo(it) }
                }

                Estrellas(museo.calificacion, museo.resenas)

                Text(museo.descripcion, style = MaterialTheme.typography.bodyLarge)
                if (museo.detalle.isNotBlank()) {
                    Text(museo.detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                InfoMuseo(museo)

                Button(
                    onClick = {
                        val busqueda = Uri.encode("${museo.tipo} ${museo.ciudad}")
                        abrir(Uri.parse("https://artsandculture.google.com/search?q=$busqueda"), "No hay un navegador instalado")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Ver proyecto virtual")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                }

                OutlinedButton(
                    onClick = { alternarFavorito() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (esFavorito) "Guardado en favoritos" else "Guardar en favoritos")
                }

                TextButton(
                    onClick = {
                        val geo = "geo:${museo.latitud},${museo.longitud}?q=${museo.latitud},${museo.longitud}(${Uri.encode(museo.nombre)})"
                        abrir(Uri.parse(geo), "No hay una app de mapas instalada")
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cómo llegar")
                }
            }
        }
    }
}

@Composable
private fun InfoMuseo(museo: Museo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FilaInfo("Tipo", museo.tipo)
            HorizontalDivider()
            FilaInfo("Ubicación", "${museo.ciudad}, ${museo.pais}")
            HorizontalDivider()
            FilaInfo("Levantamiento de datos", museo.anioLevantamiento.toString())
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Código QR", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    museo.codigoQr,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FilaInfo(titulo: String, valor: String) {
    Row {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
