package com.example.appmuseo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.repository.MuseoRepository
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.MiniaturaProducto
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.MainViewModel

@Composable
fun FavoritosScreen(
    favoritosViewModel: FavoritosViewModel,
    catalogoViewModel: CatalogoViewModel,
    mainViewModel: MainViewModel
) {
    val idsMuseos by favoritosViewModel.museos.collectAsState()
    val idsProductos by favoritosViewModel.productos.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    val todosLosMuseos = remember { MuseoRepository().obtenerMuseos() }
    val museos = todosLosMuseos.filter { it.id in idsMuseos }
    val productos = idsProductos.mapNotNull { catalogoViewModel.buscarPorId(it) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis favoritos", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = pestana) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Museos (${museos.size})") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Recursos (${productos.size})") })
            }

            if (pestana == 0) {
                if (museos.isEmpty()) {
                    SinFavoritos("Aún no guardas museos", "Toca el corazón en la ficha de un museo.", Icons.Filled.AccountBalance)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(museos, key = { it.id }) { museo ->
                            FilaFavorito(
                                titulo = museo.nombre,
                                detalle = "${museo.ciudad}, ${museo.pais}",
                                icono = { Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary) },
                                onClick = { mainViewModel.navigateTo(Screen.DetalleMuseo(museo.id)) },
                                onQuitar = { favoritosViewModel.alternarMuseo(museo.id) }
                            )
                        }
                    }
                }
            } else {
                if (productos.isEmpty()) {
                    SinFavoritos("Aún no guardas recursos", "Toca el corazón en el detalle de un producto o curso.", Icons.Filled.FavoriteBorder)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(productos, key = { it.id }) { producto ->
                            FilaFavorito(
                                titulo = producto.nombre,
                                detalle = "${producto.categoria.etiqueta} · ${formatearPrecio(producto.precio)}",
                                icono = { MiniaturaProducto(producto, tamano = 48.dp) },
                                onClick = { mainViewModel.navigateTo(Screen.Detalle(producto.id)) },
                                onQuitar = { favoritosViewModel.alternarProducto(producto.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaFavorito(
    titulo: String,
    detalle: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit,
    onQuitar: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            icono()
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onQuitar) {
                Icon(Icons.Filled.Favorite, contentDescription = "Quitar de favoritos", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SinFavoritos(titulo: String, texto: String, icono: ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(texto, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
