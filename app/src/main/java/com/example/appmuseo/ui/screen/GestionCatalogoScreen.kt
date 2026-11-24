package com.example.appmuseo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.remote.dto.ProductoDto
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.AvisoRolJwt
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.ChipsOpciones
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.components.MensajeGestion
import com.example.appmuseo.ui.components.TarjetaGestion
import com.example.appmuseo.ui.theme.AmbarPendiente
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.GestionViewModel
import com.example.appmuseo.viewmodel.MainViewModel

// Pantalla 9 del caso (rol Administrador): alta, modificación y eliminación de productos y cursos
@Composable
fun GestionCatalogoScreen(
    gestionViewModel: GestionViewModel,
    mainViewModel: MainViewModel,
    onCambios: () -> Unit = {}
) {
    val estado by gestionViewModel.estado.collectAsState()
    var categoria by remember { mutableStateOf<Categoria?>(null) }
    var porEliminar by remember { mutableStateOf<ProductoDto?>(null) }

    LaunchedEffect(Unit) { gestionViewModel.cargarProductos() }

    porEliminar?.let { producto ->
        DialogoConfirmacion(
            titulo = "Eliminar del catálogo",
            mensaje = "«${producto.nombre}» dejará de verse en la app. Esta acción no se puede deshacer.",
            icono = Icons.Filled.Delete,
            textoConfirmar = "Eliminar",
            esDestructiva = true,
            onConfirmar = {
                gestionViewModel.eliminarProducto(producto)
                porEliminar = null
                onCambios()
            },
            onCancelar = { porEliminar = null }
        )
    }

    val visibles = estado.productos.filter { categoria == null || it.categoria == categoria!!.name }

    Scaffold(
        topBar = { BarraSuperior("Gestión del catálogo", onVolver = { mainViewModel.navigateBack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    gestionViewModel.nuevoProducto()
                    mainViewModel.navigateTo(Screen.ProductoForm)
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo") },
                containerColor = DoradoBoton
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (estado.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AvisoRolJwt("Administrador · los cambios se guardan en ms-catalogo (MySQL)")
                ChipsOpciones(listOf<Categoria?>(null) + Categoria.entries, categoria, { it?.etiqueta ?: "Todos" }) { categoria = it }
                MensajeGestion(estado.mensaje) { gestionViewModel.limpiarMensaje() }
                Text("${visibles.size} productos y cursos", style = MaterialTheme.typography.labelLarge)
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visibles, key = { it.id }) { producto ->
                    val esCurso = producto.categoria == Categoria.CURSO.name
                    TarjetaGestion(
                        icono = if (esCurso) Icons.AutoMirrored.Filled.MenuBook else Icons.Filled.Inventory2,
                        titulo = producto.nombre,
                        detalle = "${formatearPrecio(producto.precio)} · " + if (esCurso) "${producto.stock} cupos" else "stock ${producto.stock}",
                        etiqueta = when {
                            producto.stock == 0 -> "Agotado"
                            producto.destacado -> "Destacado"
                            else -> null
                        },
                        colorEtiqueta = if (producto.stock == 0) AmbarPendiente else DoradoBoton
                    ) {
                        IconButton(onClick = {
                            gestionViewModel.editarProducto(producto)
                            mainViewModel.navigateTo(Screen.ProductoForm)
                        }) { Icon(Icons.Filled.Edit, contentDescription = "Editar ${producto.nombre}") }
                        IconButton(onClick = { porEliminar = producto }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${producto.nombre}", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
