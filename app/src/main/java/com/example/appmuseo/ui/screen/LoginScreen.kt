package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.R
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

/**
 * LOGIN con validaciones (Guía 11) y estados del resultado (Guía 5).
 * Las credenciales de prueba están en AuthRepository (variable, sin backend).
 */
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val estado by loginViewModel.estado.collectAsState()
    // rememberSaveable: recuerda el valor aunque se gire la pantalla (Guía 12)
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val autenticando = estado.resultado is ResultadoLogin.Autenticando

    // Cuando el login es exitoso: guardamos la sesión y vamos al Inicio (sin poder volver con "atrás")
    LaunchedEffect(estado.resultado) {
        val resultado = estado.resultado
        if (resultado is ResultadoLogin.Exito) {
            sesionViewModel.iniciarSesion(resultado.perfil)
            loginViewModel.limpiarFormulario()
            mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Login, inclusive = true)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())   // permite desplazar si aparece el teclado
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_museo),
                contentDescription = "Logo Museo de los Museos",
                modifier = Modifier.size(110.dp)
            )
            Text("Museo de los Museos", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Cursos, productos y museos virtuales de Iberoamérica en un solo lugar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Campo correo: isError + supportingText muestran el error bajo el campo (Guía 11)
            OutlinedTextField(
                value = estado.correo,
                onValueChange = loginViewModel::onCorreoChange,
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = estado.errores.correo != null,
                supportingText = {
                    estado.errores.correo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                enabled = !autenticando,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo contraseña con botón para mostrar / ocultar
            OutlinedTextField(
                value = estado.clave,
                onValueChange = loginViewModel::onClaveChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            imageVector = if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                isError = estado.errores.clave != null,
                supportingText = {
                    estado.errores.clave?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                enabled = !autenticando,
                modifier = Modifier.fillMaxWidth()
            )

            // Error que "devuelve el servidor" (credenciales incorrectas), con animación
            val resultado = estado.resultado
            AnimatedVisibility(visible = resultado is ResultadoLogin.Error) {
                Text(
                    text = (resultado as? ResultadoLogin.Error)?.mensaje ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { loginViewModel.iniciarSesion() },
                enabled = !autenticando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (autenticando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Ayuda para probar la app (MVP sin backend)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Usuarios de prueba", style = MaterialTheme.typography.titleSmall)
                    Text("admin@museo.cl · museo123", style = MaterialTheme.typography.bodyMedium)
                    Text("estudiante@museo.cl · duoc2026", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
