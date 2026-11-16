package com.example.appmuseo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.AvisoRed
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.TarjetaProducto
import com.example.appmuseo.ui.components.iconoDeCategoria
import androidx.compose.foundation.layout.size
import com.example.appmuseo.ui.utils.columnasSegunPantalla
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel,
    noLeidas: Int = 0
) {
    val estado by catalogoViewModel.estado.collectAsState()

    // precios y stock al día desde ms-catalogo
    LaunchedEffect(Unit) { catalogoViewModel.sincronizar() }
    val itemsCarrito by carritoViewModel.items.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val columnas = columnasSegunPantalla()

    // null representa la opcion "todos"
    val opcionesFiltro: List<Categoria?> = listOf<Categoria?>(null) + Categoria.entries

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Catálogo",
                cantidadCarrito = itemsCarrito.sumOf { it.cantidad },
                onCarrito = { mainViewModel.navigateTo(Screen.Carrito) }
            )
        },
        bottomBar = { BarraInferior(Screen.Catalogo.route, mainViewModel::navegarPestana, noLeidas) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AvisoRed(
                estado.red,
                onReintentar = catalogoViewModel::sincronizar,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = catalogoViewModel::onBusquedaChange,
                placeholder = { Text("Buscar productos, libros, cursos...") },
                shape = RoundedCornerShape(50),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (estado.busqueda.isNotEmpty()) {
                        IconButton(onClick = { catalogoViewModel.onBusquedaChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(opcionesFiltro) { categoria ->
                    FilterChip(
                        selected = estado.categoriaSeleccionada == categoria,
                        leadingIcon = if (categoria != null) {
                            { Icon(iconoDeCategoria(categoria), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        onClick = { catalogoViewModel.seleccionarCategoria(categoria) },
                        label = { Text(categoria?.etiqueta ?: "Todos") }
                    )
                }
            }

            val productos = estado.productosFiltrados
            if (productos.isEmpty()) {
                Text(
                    text = "No encontramos resultados para tu búsqueda.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productos, key = { it.id }) { producto ->
                        TarjetaProducto(
                            producto = producto,
                            // animateitem
                            modifier = Modifier.animateItem(),
                            onVerDetalle = { mainViewModel.navigateTo(Screen.Detalle(producto.id)) },
                            onAgregar = {
                                val agregado = carritoViewModel.agregar(producto)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (agregado) "${producto.nombre} agregado al carrito"
                                        else "No puedes agregar más unidades de este producto"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
