package com.example.appmuseo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen

private data class OpcionMenu(val screen: Screen, val etiqueta: String, val icono: ImageVector)

private val opcionesMenu = listOf(
    OpcionMenu(Screen.Home, "Inicio", Icons.Filled.Home),
    OpcionMenu(Screen.Catalogo, "Catálogo de productos y cursos", Icons.Filled.Storefront),
    OpcionMenu(Screen.Carrito, "Mi carrito", Icons.Filled.ShoppingCart),
    OpcionMenu(Screen.Cotizador, "Cotizador de envíos", Icons.Filled.Calculate),
    OpcionMenu(Screen.Escaner, "Escáner de productos", Icons.Filled.QrCodeScanner),
    OpcionMenu(Screen.Mapa, "Mapa de museos virtuales", Icons.Filled.Map),
    OpcionMenu(Screen.MisSolicitudes, "Mis solicitudes", Icons.Filled.Receipt),
    OpcionMenu(Screen.Perfil, "Mi perfil", Icons.Filled.Person)
)

@Composable
fun MenuLateral(
    nombreUsuario: String,
    onNavegar: (Screen) -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Encabezado con el nombre del usuario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Filled.Museum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Museo de los Museos", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text("Hola, $nombreUsuario", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            opcionesMenu.forEach { opcion ->
                NavigationDrawerItem(
                    label = { Text(opcion.etiqueta) },
                    icon = { Icon(opcion.icono, contentDescription = null) },
                    selected = false,
                    onClick = { onNavegar(opcion.screen) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                label = { Text("Cerrar sesión") },
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                selected = false,
                onClick = onCerrarSesion,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
