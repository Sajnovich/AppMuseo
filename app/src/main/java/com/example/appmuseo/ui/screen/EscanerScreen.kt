package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import android.content.Context
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.ui.components.CampoTexto
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

    val codigo by escanerViewModel.codigo.collectAsState()
    val errorCodigo by escanerViewModel.errorCodigo.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                "Escanea el código QR o de barras del producto con la cámara, o escríbelo a mano.",
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

            // LECTURA REAL con el lector de Google (no pide permiso de cámara: lo maneja Google Play services)
            Button(
                onClick = {
                    escanearCodigo(
                        context = context,
                        alLeer = { texto -> escanerViewModel.onCodigoLeido(texto) },
                        alFallar = { error -> escanerViewModel.onErrorEscaneo(error) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear código QR o de barras")
            }

            // Respaldo: foto + lectura simulada (útil en el emulador, que no tiene códigos reales)
            BotonesCamaraGaleria(
                onImagenObtenida = { uri -> escanerViewModel.onFotoCapturada(uri) },
                mostrarGaleria = false,
                textoCamara = "Foto del producto"
            )
            TextButton(onClick = { escanerViewModel.simularLectura() }) {
                Text("Simular lectura sin cámara (emulador)")
            }

            HorizontalDivider()

            // Ingreso manual: alternativa accesible si la cámara no está disponible
            CampoTexto(
                valor = codigo,
                onValorChange = escanerViewModel::onCodigoChange,
                etiqueta = "Código del producto",
                icono = Icons.Filled.QrCode,
                placeholder = "MDM-001",
                error = errorCodigo,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { escanerViewModel.buscarCodigo() }),
                accionFinal = {
                    IconButton(onClick = { escanerViewModel.buscarCodigo() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar código")
                    }
                }
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

/**
 * Abre el lector de códigos de Google (Google Code Scanner, ejemplo 105 del curso).
 * Google Play services muestra su propia pantalla de cámara y devuelve el texto leído,
 * por eso la app NO necesita pedir el permiso de cámara para esta función.
 */
private fun escanearCodigo(
    context: Context,
    alLeer: (String) -> Unit,
    alFallar: (String) -> Unit
) {
    val opciones = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128)
        .build()

    GmsBarcodeScanning.getClient(context, opciones)
        .startScan()
        .addOnSuccessListener { codigo -> alLeer(codigo.rawValue ?: "") }
        .addOnCanceledListener { alFallar("Escaneo cancelado.") }
        .addOnFailureListener { error -> alFallar("No se pudo escanear: ${error.message}") }
}
