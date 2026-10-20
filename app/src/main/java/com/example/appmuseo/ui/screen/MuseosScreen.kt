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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Museo
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.Estrellas
import com.example.appmuseo.ui.components.MiniaturaMuseo
import com.example.appmuseo.viewmodel.FavoritosViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.MapaViewModel

@Composable
fun MuseosScreen(
    mapaViewModel: MapaViewModel,
    favoritosViewModel: FavoritosViewModel,
    mainViewModel: MainViewModel
) {
    val estado by mapaViewModel.estado.collectAsState()
    val favoritos by favoritosViewModel.museos.collectAsState()

    // busqueda y filtro solo de esta pantalla
    var texto by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf<String?>(null) }

    val lista = estado.museos.filter { museo ->
        val coincide = texto.isBlank() ||
                museo.nombre.contains(texto, ignoreCase = true) ||
                museo.pais.contains(texto, ignoreCase = true) ||
                museo.ciudad.contains(texto, ignoreCase = true)
        coincide && (tipo == null || museo.tipo == tipo)
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Museos",
                onVolver = { mainViewModel.navigateBack() },
                acciones = {
                    IconButton(onClick = { mainViewModel.navegarPestana(Screen.Mapa) }) {
                        Icon(Icons.Filled.Map, contentDescription = "Ver en el mapa")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre, país o ciudad") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (texto.isNotEmpty()) {
                            IconButton(onClick = { texto = "" }) { Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda") }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50)
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(selected = tipo == null, onClick = { tipo = null }, label = { Text("Todos") })
                    }
                    items(estado.tipos) { t ->
                        FilterChip(selected = tipo == t, onClick = { tipo = if (tipo == t) null else t }, label = { Text(t) })
                    }
                }
            }
            item {
                Text(
                    "${lista.size} de ${estado.museos.size} museos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (lista.isEmpty()) {
                item {
                    Text(
                        "No encontramos museos con esa búsqueda",
                        modifier = Modifier.padding(top = 24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(lista, key = { it.id }) { museo ->
                FilaMuseoLista(
                    museo = museo,
                    esFavorito = museo.id in favoritos,
                    modifier = Modifier.animateItem(),
                    onClick = { mainViewModel.navigateTo(Screen.DetalleMuseo(museo.id)) },
                    onFavorito = { favoritosViewModel.alternarMuseo(museo.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaMuseoLista(
    museo: Museo,
    esFavorito: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavorito: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniaturaMuseo(museo, tamano = 64.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    museo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${museo.ciudad}, ${museo.pais} · ${museo.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Estrellas(museo.calificacion, museo.resenas)
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                    tint = if (esFavorito) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
