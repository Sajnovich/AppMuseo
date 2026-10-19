package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.R
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val estado by loginViewModel.estado.collectAsState()
    // REMEMBERSAVEABLE
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val autenticando = estado.resultado is ResultadoLogin.Autenticando

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
                .imePadding()   // deja espacio al teclado (la app es edge-to-edge)
                .verticalScroll(rememberScrollState())
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

            CampoTexto(
                valor = estado.correo,
                onValorChange = loginViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = estado.errores.correo,
                esValido = Validaciones.validarCorreo(estado.correo) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                enabled = !autenticando
            )

            CampoTexto(
                valor = estado.clave,
                onValorChange = loginViewModel::onClaveChange,
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errores.clave,
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { loginViewModel.iniciarSesion() }),
                accionFinal = {
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            imageVector = if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                enabled = !autenticando
            )

            val resultado = estado.resultado
            AnimatedVisibility(visible = resultado is ResultadoLogin.Error) {
                Text(
                    text = (resultado as? ResultadoLogin.Error)?.mensaje ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AnimatedVisibility(visible = estado.aviso != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(estado.aviso ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
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

            TextButton(onClick = { mainViewModel.navigateTo(Screen.Registro) }, enabled = !autenticando) {
                Text("¿No tienes cuenta? Crear una")
            }

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
