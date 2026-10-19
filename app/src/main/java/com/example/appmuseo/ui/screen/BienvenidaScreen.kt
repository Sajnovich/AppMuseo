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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.AzulNocheOscuro
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.ui.theme.DoradoBotonClaro
import com.example.appmuseo.viewmodel.MainViewModel

/**
 * BIENVENIDA (pantalla 1 del mockup): nombre, que ofrece la app y dos caminos:
 * crear cuenta o "ya tengo cuenta"
 */
@Composable
fun BienvenidaScreen(mainViewModel: MainViewModel) {
    // rememberSaveable: si se gira la pantalla no se repite la animacion
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulNoche, AzulNocheOscuro)))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_museo),
                contentDescription = "Logo Museo de los Museos",
                modifier = Modifier.size(110.dp)
            )
            Text(
                "Museo de los Museos",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Conecta, aprende y explora el patrimonio de Iberoamérica",
                style = MaterialTheme.typography.bodyLarge,
                color = DoradoBotonClaro,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // cada servicio entra deslizandose, uno despues del otro
            val servicios = listOf(
                Triple(Icons.Filled.Storefront, "Catálogo", "Productos, publicaciones y materiales"),
                Triple(Icons.Filled.School, "Cursos", "Formación en museología"),
                Triple(Icons.Filled.Map, "Mapa de museos", "Museos virtuales de Iberoamérica"),
                Triple(Icons.Filled.QrCodeScanner, "Escáner QR", "Abre la ficha de un museo al instante")
            )
            servicios.forEachIndexed { i, (icono, titulo, texto) ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 150 * i)) +
                            slideInVertically(tween(400, delayMillis = 150 * i)) { it }
                ) {
                    FilaServicio(icono, titulo, texto)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun FilaServicio(icono: ImageVector, titulo: String, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = DoradoBotonClaro, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(texto, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
        }
    }
}
