package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.model.Producto
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.AvatarCirculo
import com.example.appmuseo.ui.components.BarraInferior
import com.example.appmuseo.ui.components.ImagenProducto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.AzulProfundo
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.ui.theme.Terracota
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.CatalogoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.NotificacionesViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

private data class AccesoRapido(val titulo: String, val icono: ImageVector, val color: Color, val accion: () -> Unit)

private data class TarjetaExplora(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val fondo: Color,
    val textoClaro: Boolean,
    val accion: () -> Unit
)

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    sesionViewModel: SesionViewModel,
    catalogoViewModel: CatalogoViewModel,
    carritoViewModel: CarritoViewModel,
    notificacionesViewModel: NotificacionesViewModel
) {
    val usuario by sesionViewModel.usuario.collectAsState()
    val itemsCarrito by carritoViewModel.items.collectAsState()
    val notificaciones by notificacionesViewModel.notificaciones.collectAsState()
    val noLeidas = notificaciones.count { !it.leida }

    // las secciones aparecen una vez que se abre la pantalla
    var mostrar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrar = true }

    val accesos = listOf(
        AccesoRapido("Mapa", Icons.Filled.Map, Color(0xFF2E7D6B)) { mainViewModel.navegarPestana(Screen.Mapa) },
        AccesoRapido("Catálogo", Icons.Filled.Storefront, Color(0xFF2E7D6B)) {
            catalogoViewModel.seleccionarCategoria(null)
            mainViewModel.navegarPestana(Screen.Catalogo)
        },
        AccesoRapido("Escáner QR", Icons.Filled.QrCodeScanner, Color(0xFF2E7D6B)) { mainViewModel.navigateTo(Screen.Escaner) },
        AccesoRapido("Favoritos", Icons.Filled.Favorite, DoradoBoton) { mainViewModel.navigateTo(Screen.Favoritos) },
        AccesoRapido("Solicitudes", Icons.Filled.Receipt, DoradoBoton) { mainViewModel.navigateTo(Screen.MisSolicitudes) }
    )

    val explora = listOf(
        TarjetaExplora("Cursos", "Aprende con expertos", Icons.AutoMirrored.Filled.MenuBook, AzulNoche, true) {
            catalogoViewModel.seleccionarCategoria(Categoria.CURSO)
            mainViewModel.navegarPestana(Screen.Catalogo)
        },
        TarjetaExplora("Productos", "Libros y más", Icons.Filled.LocalMall, Color(0xFFEFE3CC), false) {
            catalogoViewModel.seleccionarCategoria(null)
            mainViewModel.navegarPestana(Screen.Catalogo)
        },
        TarjetaExplora("Museos virtuales", "Recorre Iberoamérica", Icons.Filled.AccountBalance, Color(0xFFCFE6E6), false) {
            mainViewModel.navigateTo(Screen.Museos)
        },
        TarjetaExplora("Novedades", "Exposiciones y eventos", Icons.Filled.Campaign, Color(0xFFE8E6E1), false) {
            mainViewModel.navegarPestana(Screen.Notificaciones)
        }
    )

    Scaffold(
        bottomBar = { BarraInferior(Screen.Home.route, mainViewModel::navegarPestana, noLeidas) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Encabezado(
                nombre = usuario.nombre,
                rol = usuario.rol.etiqueta,
                avatar = usuario.avatar,
                noLeidas = noLeidas,
                cantidadCarrito = itemsCarrito.sumOf { it.cantidad },
                onPerfil = { mainViewModel.navegarPestana(Screen.Perfil) },
                onNotificaciones = { mainViewModel.navegarPestana(Screen.Notificaciones) },
                onCarrito = { mainViewModel.navigateTo(Screen.Carrito) }
            )

            // fila de accesos rapidos, se monta encima del encabezado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-14).dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    accesos.forEach { BotonAcceso(it, Modifier.weight(1f)) }
                }
            }

            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Explora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // grilla de 2x2, cada tarjeta entra un poco despues de la anterior
                explora.chunked(2).forEachIndexed { fila, par ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        par.forEachIndexed { col, tarjeta ->
                            AnimatedVisibility(
                                visible = mostrar,
                                modifier = Modifier.weight(1f),
                                enter = fadeIn(tween(400, delayMillis = (fila * 2 + col) * 90)) +
                                        slideInVertically(tween(400, delayMillis = (fila * 2 + col) * 90)) { it / 3 }
                            ) {
                                TarjetaExploraItem(tarjeta)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Destacados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        catalogoViewModel.seleccionarCategoria(null)
                        mainViewModel.navegarPestana(Screen.Catalogo)
                    }) { Text("Ver todos") }
                }

                BannerPatrimonio(onClick = { mainViewModel.navegarPestana(Screen.Mapa) })
            }

            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(catalogoViewModel.destacados(), key = { it.id }) { producto ->
                    TarjetaDestacado(producto) { mainViewModel.navigateTo(Screen.Detalle(producto.id)) }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(
    nombre: String,
    rol: String,
    avatar: Int,
    noLeidas: Int,
    cantidadCarrito: Int,
    onPerfil: () -> Unit,
    onNotificaciones: () -> Unit,
    onCarrito: () -> Unit
) {
    // solo el primer nombre, como en el mockup
    val primerNombre = nombre.trim().substringBefore(" ").ifBlank { "visitante" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(AzulNoche, AzulProfundo)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "¡Hola, $primerNombre!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sigamos explorando el conocimiento del mundo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    rol,
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulNoche,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DoradoBotonClaro)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarCirculo(
                    indice = avatar,
                    tamano = 52.dp,
                    modifier = Modifier
                        .border(2.dp, DoradoBotonClaro, CircleShape)
                        .clickable(onClick = onPerfil)
                )
                Row {
                    IconButton(onClick = onNotificaciones) {
                        BadgedBox(badge = { if (noLeidas > 0) Badge { Text("$noLeidas") } }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones, $noLeidas sin leer", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onCarrito) {
                        BadgedBox(badge = { if (cantidadCarrito > 0) Badge { Text("$cantidadCarrito") } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito, $cantidadCarrito productos", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonAcceso(acceso: AccesoRapido, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = acceso.accion)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(acceso.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(acceso.icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            acceso.titulo,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaExploraItem(tarjeta: TarjetaExplora) {
    val colorTexto = if (tarjeta.textoClaro) Color.White else AzulNoche
    Card(
        onClick = tarjeta.accion,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tarjeta.fondo, contentColor = colorTexto)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(tarjeta.icono, contentDescription = null, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(tarjeta.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(tarjeta.subtitulo, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BannerPatrimonio(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(AzulNoche, Terracota)))
        ) {
            // icono grande de fondo, medio transparente
            Icon(
                Icons.Filled.AccountBalance,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(18.dp)
            ) {
                Text("Patrimonio que", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("nos une", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Recorre los museos del mapa →", style = MaterialTheme.typography.bodySmall, color = DoradoBotonClaro)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaDestacado(producto: Producto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ImagenProducto(
            producto = producto,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )
            Text(
                formatearPrecio(producto.precio),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
