package com.example.appmuseo.ui.screen

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Museo
import com.example.appmuseo.model.ProductoFisico
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.components.EtiquetaMuseo
import com.example.appmuseo.ui.components.MiniaturaProducto
import com.example.appmuseo.ui.components.MiniaturaMuseo
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.VerdeCompletado
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.EscanerViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch

@Composable
fun EscanerScreen(
    escanerViewModel: EscanerViewModel,
    carritoViewModel: CarritoViewModel,
    mainViewModel: MainViewModel
) {
    val producto by escanerViewModel.productoDetectado.collectAsState()
    val museo by escanerViewModel.museoDetectado.collectAsState()
    val codigo by escanerViewModel.codigo.collectAsState()
    val errorCodigo by escanerViewModel.errorCodigo.collectAsState()
    val mensaje by escanerViewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val hayResultado = museo != null || producto != null

    // rememberSaveable: al volver desde la ficha no se abre la camara de nuevo
    var primeraVez by rememberSaveable { mutableStateOf(true) }
    // sin camara (o si falla) se puede escribir el codigo a mano
    var modoManual by rememberSaveable { mutableStateOf(false) }

    fun abrirCamara() {
        // si venia de escribir el codigo, al cancelar vuelve ahi y no se sale
        val veniaDeManual = modoManual
        modoManual = false
        escanearCodigo(
            context = context,
            alLeer = { texto ->
                escanerViewModel.onCodigoLeido(texto)
                // codigo que no es de la app: se muestra el error con el campo manual
                if (escanerViewModel.museoDetectado.value == null && escanerViewModel.productoDetectado.value == null) {
                    modoManual = true
                }
            },
            alCancelar = {
                // si cierra la camara sin escanear, vuelve a donde estaba
                if (veniaDeManual) {
                    modoManual = true
                } else if (escanerViewModel.museoDetectado.value == null && escanerViewModel.productoDetectado.value == null) {
                    mainViewModel.navigateBack()
                }
            },
            alFallar = { error ->
                escanerViewModel.onErrorEscaneo(error)
                modoManual = true
            }
        )
    }

    // al entrar desde "Escáner QR" se abre la camara altiro
    LaunchedEffect(Unit) {
        if (primeraVez) {
            primeraVez = false
            escanerViewModel.reiniciar()
            abrirCamara()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (hayResultado) "Resultado del QR" else "Escáner QR",
                onVolver = {
                    escanerViewModel.reiniciar()
                    mainViewModel.navigateBack()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                hayResultado -> ResultadoCodigo(
                    museo = museo,
                    producto = producto,
                    onVerDetalle = {
                        museo?.let { mainViewModel.navigateTo(Screen.DetalleMuseo(it.id)) }
                        producto?.let { mainViewModel.navigateTo(Screen.Detalle(it.id)) }
                    },
                    onAgregar = { p ->
                        val agregado = carritoViewModel.agregar(p)
                        scope.launch {
                            snackbarHostState.showSnackbar(if (agregado) "Agregado al carrito" else "Sin stock disponible")
                        }
                    },
                    onVolverAEscanear = {
                        escanerViewModel.reiniciar()
                        abrirCamara()
                    }
                )

                modoManual -> EntradaManual(
                    codigo = codigo,
                    error = errorCodigo,
                    mensaje = mensaje,
                    onCodigoChange = escanerViewModel::onCodigoChange,
                    onBuscar = { escanerViewModel.buscarCodigo() },
                    onSimularMuseo = { escanerViewModel.simularLecturaMuseo((1..14).random()) },
                    onSimularProducto = { escanerViewModel.simularLectura() },
                    onAbrirCamara = { abrirCamara() }
                )

                // mientras la camara esta abierta encima, solo un fondo simple
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = DoradoBoton)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Abriendo la cámara…", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { modoManual = true }) { Text("¿Sin cámara? Escribir el código") }
                }
            }
        }
    }
}

// respaldo para el emulador o si la camara falla
@Composable
private fun EntradaManual(
    codigo: String,
    error: String?,
    mensaje: String?,
    onCodigoChange: (String) -> Unit,
    onBuscar: () -> Unit,
    onSimularMuseo: () -> Unit,
    onSimularProducto: () -> Unit,
    onAbrirCamara: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.QrCode2, contentDescription = null, tint = AzulNoche, modifier = Modifier.size(72.dp))
        Text("Escribe el código", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Está impreso bajo cada QR: MUSEO-1 a MUSEO-14 o MDM-001 a MDM-009.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }

        CampoTexto(
            valor = codigo,
            onValorChange = onCodigoChange,
            etiqueta = "Código",
            icono = Icons.Filled.QrCode,
            placeholder = "MUSEO-1 o MDM-001",
            error = error,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
            accionFinal = {
                IconButton(onClick = onBuscar) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar código")
                }
            }
        )

        Button(
            onClick = onAbrirCamara,
            colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Abrir la cámara")
        }

        HorizontalDivider()
        Text("Para probar en el emulador", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onSimularMuseo) { Text("Simular QR de museo") }
            TextButton(onClick = onSimularProducto) { Text("Simular producto") }
        }
    }
}

@Composable
private fun ResultadoCodigo(
    museo: Museo?,
    producto: ProductoFisico?,
    onVerDetalle: () -> Unit,
    onAgregar: (ProductoFisico) -> Unit,
    onVolverAEscanear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // el check entra con rebote
        val escala = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        Box(
            modifier = Modifier
                .size((84 * escala.value).dp)
                .clip(CircleShape)
                .background(VerdeCompletado),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size((56 * escala.value).dp))
        }

        Text("¡Código identificado!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Hemos encontrado la siguiente información:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (museo != null) MiniaturaMuseo(museo, tamano = 84.dp)
                    if (producto != null) MiniaturaProducto(producto, tamano = 84.dp)
                    Column(modifier = Modifier.padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            museo?.nombre ?: producto?.nombre.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        EtiquetaMuseo(if (museo != null) "Museo · ${museo.tipo}" else producto?.categoria?.etiqueta.orEmpty())
                        Text(
                            museo?.codigoQr ?: producto?.codigoBarra.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
                if (museo != null) {
                    Text("${museo.ciudad}, ${museo.pais}", style = MaterialTheme.typography.labelLarge)
                    Text(museo.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
                if (producto != null) {
                    Text("${formatearPrecio(producto.precio)} · ${producto.disponibilidad()}", style = MaterialTheme.typography.labelLarge)
                    Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Button(
            onClick = onVerDetalle,
            colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Ver detalle")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        if (producto != null) {
            OutlinedButton(onClick = { onAgregar(producto) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(producto.etiquetaAccion())
            }
        }
        OutlinedButton(onClick = onVolverAEscanear, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver a escanear")
        }
    }
}

private fun escanearCodigo(
    context: Context,
    alLeer: (String) -> Unit,
    alCancelar: () -> Unit,
    alFallar: (String) -> Unit
) {
    val opciones = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128)
        .build()

    GmsBarcodeScanning.getClient(context, opciones)
        .startScan()
        .addOnSuccessListener { codigo -> alLeer(codigo.rawValue ?: "") }
        .addOnCanceledListener { alCancelar() }
        .addOnFailureListener { error -> alFallar("No se pudo abrir la cámara: ${error.message}") }
}
