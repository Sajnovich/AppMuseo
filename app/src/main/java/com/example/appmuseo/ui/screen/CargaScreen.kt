package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.AzulNocheOscuro
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SPLASH animado en 3 tiempos:
 * 1) el logo entra GRANDE y se achica a su tamaño
 * 2) aparece la barra de carga
 * 3) el logo da dos saltos con rebote y recien ahi se navega
 */
@Composable
fun CargaScreen(sesionViewModel: SesionViewModel, mainViewModel: MainViewModel) {
    val sesionActiva by sesionViewModel.sesionActiva.collectAsState()

    // Animatable = valor animado que controlamos paso a paso desde una corrutina
    val escala = remember { Animatable(2.4f) }
    val alfa = remember { Animatable(0f) }
    val salto = remember { Animatable(0f) }
    var mostrarTexto by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var animacionLista by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { alfa.animateTo(1f, tween(600)) }
        escala.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))   // grande → normal
        mostrarTexto = true
        cargando = true
        delay(1000)
        cargando = false
        repeat(2) {
            salto.animateTo(-40f, tween(200, easing = FastOutSlowInEasing))   // sube
            salto.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))   // cae rebotando
        }
        animacionLista = true
    }

    // se navega solo cuando termino la animacion Y ya se sabe si hay sesion
    LaunchedEffect(animacionLista, sesionActiva) {
        if (!animacionLista) return@LaunchedEffect
        when (sesionActiva) {
            true -> mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Carga, inclusive = true)
            false -> mainViewModel.navigateTo(Screen.Bienvenida, popUpToRoute = Screen.Carga, inclusive = true)
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulNoche, AzulNocheOscuro))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_museo),
                contentDescription = "Logo Museo de los Museos",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = escala.value
                        scaleY = escala.value
                        alpha = alfa.value
                        translationY = salto.value.dp.toPx()
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(visible = mostrarTexto, enter = fadeIn() + slideInVertically { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Museo de los Museos", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(
                        "Patrimonio de Iberoamérica",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoradoBotonClaro
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(visible = cargando, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator(
                    modifier = Modifier.width(140.dp),
                    color = DoradoBoton,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}
