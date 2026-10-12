package com.example.appmuseo.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Museo
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.utils.ProyeccionMapa
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel

/**
 * MAPA DE MUSEOS VIRTUALES DE IBEROAMÉRICA.
 * Es un mapa SIMULADO: un rectángulo donde cada museo se ubica según su latitud/longitud
 * (ver ProyeccionMapa). En la Unidad 3 se puede reemplazar por Google Maps con el backend.
 * Además, el botón "Abrir en mapas" usa un Intent para abrir la app de mapas del teléfono.
 */
@Composable
fun MapaMuseosScreen(mapaViewModel: MapaViewModel, mainViewModel: MainViewModel) {
    val estado by mapaViewModel.estado.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mapa de museos virtuales") },
        bottomBar = { BarraInferior(Screen.Mapa.route, mainViewModel::navegarPestana) }
    ) { innerPadding ->
        // Toda la pantalla es una sola LazyColumn: filtros, mapa, detalle y lista
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1) Filtro por país
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = estado.paisSeleccionado == null,
                            onClick = { mapaViewModel.seleccionarPais(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(estado.paises) { pais ->
                        FilterChip(
                            selected = estado.paisSeleccionado == pais,
                            onClick = { mapaViewModel.seleccionarPais(pais) },
                            label = { Text(pais) }
                        )
                    }
                }
            }

            // 2) Mapa simulado
            item {
                MapaSimulado(
                    museos = estado.museosVisibles,
                    seleccionado = estado.museoSeleccionado,
                    onSeleccionar = { mapaViewModel.seleccionarMuseo(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3) Detalle del museo seleccionado (aparece con animación)
            item {
                AnimatedVisibility(visible = estado.museoSeleccionado != null) {
                    estado.museoSeleccionado?.let { museo ->
                        DetalleMuseo(
                            museo = museo,
                            onAbrirMapa = {
                                // Intent implícito: Android busca una app que sepa abrir "geo:"
                                val uri = Uri.parse("geo:${museo.latitud},${museo.longitud}?q=${museo.latitud},${museo.longitud}(${Uri.encode(museo.nombre)})")
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e: ActivityNotFoundException) {
                                    // Control de errores: si no hay app de mapas, avisamos en vez de cerrarse
                                    Toast.makeText(context, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // 4) Lista de museos
            item {
                Text(
                    "Museos (${estado.museosVisibles.size}) · datos sintéticos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(estado.museosVisibles, key = { it.id }) { museo ->
                FilaMuseo(
                    museo = museo,
                    seleccionado = estado.museoSeleccionado?.id == museo.id,
                    onClick = { mapaViewModel.seleccionarMuseo(museo) }
                )
            }
        }
    }
}

/** Rectángulo "mapa" con un marcador por museo. */
@Composable
private fun MapaSimulado(
    museos: List<Museo>,
    seleccionado: Museo?,
    onSeleccionar: (Museo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        // BoxWithConstraints nos da el ancho y alto disponibles (maxWidth / maxHeight) en dp
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            val ancho = maxWidth
            val alto = maxHeight
            // Márgenes internos: sin ellos, los marcadores de los bordes (Madrid, Lisboa) quedaban cortados
            val margenX = 24.dp
            val margenSuperior = 44.dp   // alto del marcador seleccionado + un poco de aire
            val margenInferior = 28.dp   // espacio para la leyenda de abajo
            val anchoUtil = ancho - margenX * 2
            val altoUtil = alto - margenSuperior - margenInferior

            // Etiquetas decorativas de los océanos
            Text(
                "Océano Pacífico",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.offset(x = ancho * 0.05f, y = alto * 0.62f)
            )
            Text(
                "Océano Atlántico",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.offset(x = ancho * 0.62f, y = alto * 0.45f)
            )

            museos.forEach { museo ->
                // key: le dice a Compose qué marcador es cuál, aunque la lista cambie al filtrar
                key(museo.id) {
                    val (x, y) = ProyeccionMapa.aPosicionRelativa(museo.latitud, museo.longitud)
                    val estaSeleccionado = museo.id == seleccionado?.id
                    // El marcador seleccionado crece con animación
                    val tamanoMarcador by animateDpAsState(
                        targetValue = if (estaSeleccionado) 40.dp else 28.dp,
                        label = "tamanoMarcador"
                    )
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "${museo.nombre}, ${museo.ciudad}",
                        tint = if (estaSeleccionado) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            // La punta del ícono debe quedar sobre la coordenada: restamos medio ancho y el alto
                            .offset(
                                x = margenX + anchoUtil * x - tamanoMarcador / 2,
                                y = margenSuperior + altoUtil * y - tamanoMarcador
                            )
                            .size(tamanoMarcador)
                            .clickable { onSeleccionar(museo) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vista esquemática · toca un marcador", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DetalleMuseo(museo: Museo, onAbrirMapa: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(museo.nombre, style = MaterialTheme.typography.titleLarge)
            Text("${museo.ciudad}, ${museo.pais} · ${museo.tipo}", style = MaterialTheme.typography.bodyMedium)
            Text(museo.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Lat ${museo.latitud} · Lon ${museo.longitud} · Levantamiento ${museo.anioLevantamiento}",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = onAbrirMapa) {
                Icon(Icons.Filled.Place, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir en la app de mapas")
            }
        }
    }
}

@Composable
private fun FilaMuseo(museo: Museo, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (seleccionado) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Museum, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(museo.nombre, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${museo.ciudad}, ${museo.pais}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
