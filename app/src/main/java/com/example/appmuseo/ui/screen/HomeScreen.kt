package com.example.appmuseo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.MenuLateral
import com.example.appmuseo.ui.components.TarjetaProducto
import com.example.appmuseo.ui.utils.obtenerWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

// Acceso rápido del inicio (texto + ícono + a qué acción lleva)
private data class AccesoRapido(val titulo: String, val icono: ImageVector, val accion: () -> Unit)

/**
 * INICIO: menú lateral (ModalNavigationDrawer) + TopAppBar + NavigationBar (Guía 10).
 * Los accesos rápidos se acomodan en 2, 3 o 4 columnas según el ancho (Guía 9).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    sesionViewModel: SesionViewModel,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val itemsCarrito by carritoViewModel.items.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val columnas = when (obtenerWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        else -> 4
    }

    val accesos = listOf(
        AccesoRapido("Catálogo", Icons.Filled.Storefront) {
            catalogoViewModel.seleccionarCategoria(null)
            mainViewModel.navegarPestana(Screen.Catalogo)
        },
        AccesoRapido("Cursos", Icons.Filled.School) {
            catalogoViewModel.seleccionarCategoria(Categoria.CURSO)   // abre el catálogo ya filtrado
            mainViewModel.navegarPestana(Screen.Catalogo)
        },
        AccesoRapido("Mapa de museos", Icons.Filled.Map) { mainViewModel.navegarPestana(Screen.Mapa) },
        AccesoRapido("Cotizar envío", Icons.Filled.Calculate) { mainViewModel.navigateTo(Screen.Cotizador) },
        AccesoRapido("Escanear", Icons.Filled.QrCodeScanner) { mainViewModel.navigateTo(Screen.Escaner) },
        AccesoRapido("Mis solicitudes", Icons.Filled.Receipt) { mainViewModel.navegarPestana(Screen.MisSolicitudes) }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MenuLateral(
                nombreUsuario = usuario.nombre,
                onNavegar = { screen ->
                    scope.launch { drawerState.close() }
                    when (screen) {
                        Screen.Home -> Unit   // ya estamos en el Inicio
                        // Las pantallas de la barra inferior se abren como pestañas (sin duplicarlas en la pila)
                        Screen.Catalogo, Screen.Mapa, Screen.MisSolicitudes, Screen.Perfil -> mainViewModel.navegarPestana(screen)
                        else -> mainViewModel.navigateTo(screen)
                    }
                },
                onCerrarSesion = {
                    scope.launch { drawerState.close() }
                    sesionViewModel.cerrarSesion()
                    carritoViewModel.vaciar()
                    mainViewModel.navigateTo(Screen.Login, popUpToRoute = Screen.Home, inclusive = true)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = "Museo de los Museos",
                    onMenu = { scope.launch { drawerState.open() } },
                    cantidadCarrito = itemsCarrito.sumOf { it.cantidad },
                    onCarrito = { mainViewModel.navigateTo(Screen.Carrito) }
                )
            },
            bottomBar = { BarraInferior(Screen.Home.route, mainViewModel::navegarPestana) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Saludo
                Text("Hola, ${usuario.nombre.ifBlank { "visitante" }}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Perfil: ${usuario.tipoUsuario.ifBlank { "Público general" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Banner principal
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Tu museo en el bolsillo", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Inscríbete en cursos, compra productos museológicos y explora el mapa de museos virtuales de Iberoamérica.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Accesos rápidos en grilla: chunked divide la lista en filas de "columnas" elementos
                Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
                accesos.chunked(columnas).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        fila.forEach { acceso ->
                            TarjetaAcceso(acceso, Modifier.weight(1f))
                        }
                        // Si la última fila queda incompleta, rellenamos con espacios
                        repeat(columnas - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }

                // Productos destacados (reutiliza TarjetaProducto)
                Text("Destacados", style = MaterialTheme.typography.titleMedium)
                catalogoViewModel.destacados().forEach { producto ->
                    TarjetaProducto(
                        producto = producto,
                        onVerDetalle = { mainViewModel.navigateTo(Screen.Detalle(producto.id)) },
                        onAgregar = {
                            val agregado = carritoViewModel.agregar(producto)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (agregado) "${producto.nombre} agregado al carrito" else "No quedan más unidades disponibles"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaAcceso(acceso: AccesoRapido, modifier: Modifier = Modifier) {
    Card(
        onClick = acceso.accion,
        modifier = modifier.heightIn(min = 104.dp),   // heightIn: crece si el usuario usa letra grande
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(acceso.icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(acceso.titulo, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}
