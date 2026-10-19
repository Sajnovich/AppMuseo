package com.example.appmuseo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.appmuseo.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    cantidadCarrito: Int = 0,
    onCarrito: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(text = titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            } else if (onMenu != null) {
                IconButton(onClick = onMenu) {
                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                }
            }
        },
        actions = {
            if (onCarrito != null) {
                IconButton(onClick = onCarrito) {
                    BadgedBox(badge = { if (cantidadCarrito > 0) Badge { Text("$cantidadCarrito") } }) {
                        // el contentdescription incluye la cantidad
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito, $cantidadCarrito productos")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

private data class Pestana(val screen: Screen, val etiqueta: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana(Screen.Home, "Inicio", Icons.Filled.Home),
    Pestana(Screen.Catalogo, "Catálogo", Icons.Filled.Storefront),
    Pestana(Screen.Mapa, "Mapa", Icons.Filled.Map),
    Pestana(Screen.MisSolicitudes, "Solicitudes", Icons.Filled.Receipt),
    Pestana(Screen.Perfil, "Perfil", Icons.Filled.Person)
)

@Composable
fun BarraInferior(rutaActual: String, onNavegar: (Screen) -> Unit) {
    NavigationBar {
        pestanas.forEach { pestana ->
            NavigationBarItem(
                selected = rutaActual == pestana.screen.route,
                onClick = { if (rutaActual != pestana.screen.route) onNavegar(pestana.screen) },
                // contentDescription = null porque la etiqueta de texto
                icon = { Icon(pestana.icono, contentDescription = null) },
                label = { Text(pestana.etiqueta, maxLines = 1) }
            )
        }
    }
}
