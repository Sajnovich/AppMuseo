package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

/**
 * PANTALLA DE CARGA (Guía 12): muestra un CircularProgressIndicator mientras se lee
 * DataStore. Si había una sesión guardada va directo al Inicio; si no, al Login.
 */
@Composable
fun CargaScreen(sesionViewModel: SesionViewModel, mainViewModel: MainViewModel) {
    val sesionActiva by sesionViewModel.sesionActiva.collectAsState()
    var mostrarLogo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrarLogo = true }   // al entrar, se activa la animación

    // LaunchedEffect se ejecuta cada vez que cambia "sesionActiva"
    LaunchedEffect(sesionActiva) {
        when (sesionActiva) {
            true -> mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Carga, inclusive = true)
            false -> mainViewModel.navigateTo(Screen.Login, popUpToRoute = Screen.Carga, inclusive = true)
            null -> Unit // todavía cargando
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // El logo aparece agrandándose y con fundido (animación de entrada)
            AnimatedVisibility(
                visible = mostrarLogo,
                enter = fadeIn(tween(700)) + scaleIn(initialScale = 0.6f, animationSpec = tween(700))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_museo),
                    contentDescription = "Logo Museo de los Museos",
                    modifier = Modifier.size(140.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Museo de los Museos", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}
