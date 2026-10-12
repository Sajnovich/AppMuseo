package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.BotonesCamaraGaleria
import com.example.appmuseo.ui.components.ImagenInteligente
import com.example.appmuseo.ui.components.TarjetaProducto
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * ESCÁNER DE PRODUCTOS (simulado). Usa la CÁMARA real para la foto (recurso nativo, Guía 13)
 * y simula la lectura del código de barras. También permite escribir el código a mano.
 */
@Composable
fun EscanerScreen(
    escanerViewModel: EscanerViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel
) {
    val fotoUri by escanerViewModel.fotoUri.collectAsState()
    val producto by escanerViewModel.productoDetectado.collectAsState()
    val mensaje by escanerViewModel.mensaje.collectAsState()
    val itemsCarrito by carritoViewModel.items.collectAsState()

    // Texto del campo manual: estado LOCAL de la pantalla (no necesita ViewModel)
    var codigoManual by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Escáner de productos",
                onVolver = { mainViewModel.navigateBack() },
                cantidadCarrito = itemsCarrito.sumOf { it.cantidad },
                onCarrito = { mainViewModel.navigateTo(Screen.Carrito) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()   // el teclado no tapa el campo del código
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Toma una foto del código de barras del producto. La lectura del código es simulada.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // Reutilizamos el componente de la Guía 13
            ImagenInteligente(
                uri = fotoUri,
                tamano = 160.dp,
                descripcion = "Foto del producto escaneado",
                iconoPorDefecto = Icons.Filled.QrCodeScanner
            )

            // Solo cámara (sin galería): el escaneo se hace en el momento
            BotonesCamaraGaleria(
                onImagenObtenida = { uri -> escanerViewModel.onFotoCapturada(uri) },
                mostrarGaleria = false,
                textoCamara = "Escanear con cámara"
            )
            TextButton(onClick = { escanerViewModel.simularLectura() }) {
                Text("Simular lectura sin cámara (emulador)")
            }

            HorizontalDivider()

            // Ingreso manual: alternativa accesible si la cámara no está disponible
            OutlinedTextField(
                value = codigoManual,
                onValueChange = { codigoManual = it },
                label = { Text("Código manual (ej: MDM-001)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { escanerViewModel.buscarCodigo(codigoManual) }),
                trailingIcon = {
                    IconButton(onClick = { escanerViewModel.buscarCodigo(codigoManual) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar código")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            mensaje?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Producto detectado (reutiliza TarjetaProducto)
            AnimatedVisibility(visible = producto != null) {
                producto?.let { p ->
                    TarjetaProducto(
                        producto = p,
                        onVerDetalle = { mainViewModel.navigateTo(Screen.Detalle(p.id)) },
                        onAgregar = {
                            val agregado = carritoViewModel.agregar(p)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (agregado) "Agregado al carrito" else "Sin stock disponible")
                            }
                        }
                    )
                }
            }
        }
    }
}
