package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.theme.AzulNocheOscuro
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.viewmodel.MainViewModel

/**
 * BIENVENIDA (pantalla 1 del mockup): imagen de una galeria de fondo, el nombre
 * y dos caminos: crear cuenta o "ya tengo cuenta". Lo demas se ve despues de entrar
 */
@Composable
fun BienvenidaScreen(mainViewModel: MainViewModel) {
    // rememberSaveable: si se gira la pantalla no se repite la animacion
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize().background(AzulNocheOscuro)) {
        Image(
            painter = painterResource(id = R.drawable.bienvenida_fondo),
            contentDescription = null,   // es decorativa
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // degradado oscuro arriba y abajo para que se lean el titulo y los botones
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to AzulNocheOscuro.copy(alpha = 0.85f),
                        0.35f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1f to AzulNocheOscuro
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo_museo),
                        contentDescription = "Logo Museo de los Museos",
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Museo de los Museos",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Conecta, aprende y explora el patrimonio de Iberoamérica",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DoradoBotonClaro,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // los botones suben un poco despues del titulo
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400)) { it }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { mainViewModel.navigateTo(Screen.Registro) },
                        colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Crear cuenta", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = { mainViewModel.navigateTo(Screen.Login) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Ya tengo cuenta", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
