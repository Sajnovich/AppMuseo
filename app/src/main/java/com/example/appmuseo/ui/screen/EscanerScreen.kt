package com.example.appmuseo.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.appmuseo.ui.components.BotonesCamaraGaleria
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.components.EtiquetaMuseo
import com.example.appmuseo.ui.components.IconoCategoria
import com.example.appmuseo.ui.components.MiniaturaMuseo
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.AzulProfundo
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
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

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (hayResultado) "Resultado del QR" else "Escáner QR",
                onVolver = {
                    if (hayResultado) escanerViewModel.reiniciar() else mainViewModel.navigateBack()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // cambia entre "escanear" y "resultado" con una transicion
        AnimatedContent(
            targetState = hayResultado,
            transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f)) togetherWith fadeOut(tween(200)) },
            label = "escaner",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { mostrarResultado ->
            if (mostrarResultado) {
                ResultadoCodigo(
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
                    onVolverAEscanear = { escanerViewModel.reiniciar() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    PanelEscaneo(
                        onEscanear = {
                            escanearCodigo(
                                context = context,
                                alLeer = { texto -> escanerViewModel.onCodigoLeido(texto) },
                                alFallar = { error -> escanerViewModel.onErrorEscaneo(error) }
                            )
                        }
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) }

                        BotonesCamaraGaleria(
                            onImagenObtenida = { uri -> escanerViewModel.onFotoCapturada(uri) },
                            mostrarGaleria = false,
                            textoCamara = "Foto del producto"
                        )

                        // para el emulador, que no tiene camara real
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { escanerViewModel.simularLecturaMuseo((1..14).random()) }) {
                                Text("Simular QR de museo")
                            }
                            TextButton(onClick = { escanerViewModel.simularLectura() }) {
                                Text("Simular producto")
                            }
                        }

                        HorizontalDivider()

                        CampoTexto(
                            valor = codigo,
                            onValorChange = escanerViewModel::onCodigoChange,
                            etiqueta = "Escribe el código",
                            icono = Icons.Filled.QrCode,
                            placeholder = "MUSEO-1 o MDM-001",
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
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelEscaneo(onEscanear: () -> Unit) {
    // linea que baja y sube dentro del marco
    val transicion = rememberInfiniteTransition(label = "linea")
    val avance by transicion.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "avance"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(AzulNoche, AzulProfundo)))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Escáner QR", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Escanea códigos de museos, publicaciones o productos.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(210.dp)
                .drawBehind {
                    // esquinas del marco
                    val largo = size.minDimension * 0.18f
                    val grosor = 5.dp.toPx()
                    val color = DoradoBotonClaro
                    val w = size.width
                    val h = size.height
                    listOf(
                        Offset(0f, 0f) to Pair(Offset(largo, 0f), Offset(0f, largo)),
                        Offset(w, 0f) to Pair(Offset(w - largo, 0f), Offset(w, largo)),
                        Offset(0f, h) to Pair(Offset(largo, h), Offset(0f, h - largo)),
                        Offset(w, h) to Pair(Offset(w - largo, h), Offset(w, h - largo))
                    ).forEach { (esquina, extremos) ->
                        drawLine(color, esquina, extremos.first, grosor, StrokeCap.Round)
                        drawLine(color, esquina, extremos.second, grosor, StrokeCap.Round)
                    }
                    val y = h * avance
                    drawLine(Color(0xFF4CAF50), Offset(w * 0.08f, y), Offset(w * 0.92f, y), 3.dp.toPx())
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.QrCode2, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(150.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onEscanear,
            colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear con la cámara")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Coloca el código dentro del marco y se leerá automáticamente.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
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
                    if (producto != null) IconoCategoria(producto.categoria, tamano = 84.dp)
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
