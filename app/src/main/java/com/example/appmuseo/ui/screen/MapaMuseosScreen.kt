package com.example.appmuseo.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.MuseoCercano
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.AvisoRed
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.Estrellas
import com.example.appmuseo.ui.components.EtiquetaMuseo
import com.example.appmuseo.ui.components.MapaMuseosOsm
import com.example.appmuseo.ui.components.MiniaturaMuseo
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.utils.formatearDistancia
import com.example.appmuseo.utils.obtenerUltimaUbicacion
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel

private val FILTROS = listOf("Todos", "País", "Proyecto")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaMuseosScreen(mapaViewModel: MapaViewModel, mainViewModel: MainViewModel, noLeidas: Int = 0) {
    val estado by mapaViewModel.estado.collectAsState()
    val context = LocalContext.current

    // al abrir el mapa se piden los museos al backend
    LaunchedEffect(Unit) { mapaViewModel.sincronizar() }

    // 0 = todos, 1 = por pais, 2 = por proyecto
    var filtro by rememberSaveable { mutableIntStateOf(0) }

    fun usarUbicacion() {
        val ubicacion = obtenerUltimaUbicacion(context)
        mapaViewModel.buscarMuseoCercano(ubicacion?.latitude, ubicacion?.longitude)
        filtro = 0
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (!concedido) {
            Toast.makeText(context, "Sin permiso de ubicación usamos Santiago como referencia", Toast.LENGTH_SHORT).show()
        }
        usarUbicacion()
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mapa de museos",
                acciones = {
                    IconButton(onClick = { mainViewModel.navigateTo(Screen.Museos) }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ver lista de museos")
                    }
                }
            )
        },
        bottomBar = { BarraInferior(Screen.Mapa.route, mainViewModel::navegarPestana, noLeidas) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // el mapa ocupa toda la pantalla y lo demas va encima
            MapaMuseosOsm(
                museos = estado.museosVisibles,
                seleccionado = estado.museoSeleccionado,
                onSeleccionar = { mapaViewModel.seleccionarMuseo(it) },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                AvisoRed(estado.red, onReintentar = mapaViewModel::sincronizar)
                Surface(shape = RoundedCornerShape(50), shadowElevation = 4.dp) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(4.dp)) {
                        FILTROS.forEachIndexed { i, texto ->
                            SegmentedButton(
                                selected = filtro == i,
                                onClick = {
                                    filtro = i
                                    if (i == 0) mapaViewModel.limpiarFiltros()
                                },
                                shape = SegmentedButtonDefaults.itemShape(i, FILTROS.size)
                            ) { Text(texto) }
                        }
                    }
                }

                // chips de pais o de proyecto segun el filtro elegido
                AnimatedVisibility(
                    visible = filtro != 0,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val opciones = if (filtro == 1) estado.paises else estado.tipos
                    val elegido = if (filtro == 1) estado.paisSeleccionado else estado.tipoSeleccionado
                    LazyRow(
                        contentPadding = PaddingValues(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(opciones) { opcion ->
                            FilterChip(
                                selected = elegido == opcion,
                                onClick = {
                                    val nuevo = if (elegido == opcion) null else opcion
                                    if (filtro == 1) mapaViewModel.seleccionarPais(nuevo) else mapaViewModel.seleccionarTipo(nuevo)
                                },
                                label = { Text(opcion) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    selectedContainerColor = DoradoBotonClaro
                                )
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (tienePermiso) usarUbicacion() else lanzadorPermiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Museo más cercano a mí")
                }
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = estado.museoSeleccionado != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    estado.museoSeleccionado?.let { museo ->
                        TarjetaMuseoMapa(
                            museo = museo,
                            cercano = estado.cercano?.takeIf { it.museo.id == museo.id },
                            onVerDetalle = { mainViewModel.navigateTo(Screen.DetalleMuseo(museo.id)) }
                        )
                    }
                }
            }

            if (estado.museosVisibles.isEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Text("No hay museos con ese filtro", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaMuseoMapa(museo: Museo, cercano: MuseoCercano?, onVerDetalle: () -> Unit) {
    Card(
        onClick = onVerDetalle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniaturaMuseo(museo, tamano = 76.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    museo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.padding(end = 2.dp).width(14.dp))
                    Text("${museo.ciudad}, ${museo.pais}", style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    EtiquetaMuseo(museo.tipo)
                    Spacer(modifier = Modifier.width(6.dp))
                    EtiquetaMuseo("Virtual")
                }
                Estrellas(museo.calificacion, museo.resenas)
                cercano?.let {
                    Text(
                        if (it.ubicacionAproximada) "A ${formatearDistancia(it.distanciaKm)} de Santiago (referencia)"
                        else "A ${formatearDistancia(it.distanciaKm)} de ti",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Ver detalle")
        }
    }
}
