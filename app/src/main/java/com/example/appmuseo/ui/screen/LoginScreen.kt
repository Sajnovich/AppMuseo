package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.ResultadoLogin
import com.example.appmuseo.model.Rol
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.repository.AuthRepository
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

private fun iconoDe(rol: Rol): ImageVector = when (rol) {
    Rol.ADMINISTRADOR -> Icons.Filled.AdminPanelSettings
    Rol.INVESTIGADOR -> Icons.Filled.Science
    Rol.CLIENTE -> Icons.Filled.Person
}

/**
 * INICIO DE SESION en 2 pasos (pantalla 3 del mockup):
 * 1) elegir el tipo de cuenta  2) correo y contraseña de ese rol
 */
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val estado by loginViewModel.estado.collectAsState()
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val autenticando = estado.resultado is ResultadoLogin.Autenticando

    LaunchedEffect(estado.resultado) {
        val resultado = estado.resultado
        if (resultado is ResultadoLogin.Exito) {
            sesionViewModel.iniciarSesion(resultado.perfil)
            loginViewModel.limpiarFormulario()
            // se borra bienvenida y login del historial: "atras" no vuelve aca
            mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Bienvenida, inclusive = true)
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "",
                onVolver = {
                    // en el paso 2, "volver" regresa a elegir el rol
                    if (estado.rolSeleccionado != null) loginViewModel.onRolChange(null) else mainViewModel.navigateBack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)

            // AnimatedContent: cambia entre "elegir rol" y "formulario" deslizando
            AnimatedContent(
                targetState = estado.rolSeleccionado,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "pasoLogin"
            ) { rol ->
                if (rol == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Selecciona tu tipo de cuenta",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Rol.entries.forEach { opcion ->
                            TarjetaRol(opcion) { loginViewModel.onRolChange(opcion) }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AssistChip(
                            onClick = { loginViewModel.onRolChange(null) },
                            label = { Text("${rol.etiqueta} · cambiar") },
                            leadingIcon = { Icon(iconoDe(rol), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
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
                                        if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            enabled = !autenticando
                        )

                        val resultado = estado.resultado
                        AnimatedVisibility(visible = resultado is ResultadoLogin.Error) {
                            Text(
                                (resultado as? ResultadoLogin.Error)?.mensaje ?: "",
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (autenticando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        // cuentas de prueba del rol elegido (credenciales ficticias)
                        val pruebas = AuthRepository.USUARIOS_PRUEBA.filter { it.perfil.rol == rol }
                        if (pruebas.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Cuenta de prueba", style = MaterialTheme.typography.titleSmall)
                                    pruebas.forEach { Text("${it.correo} · ${it.clave}", style = MaterialTheme.typography.bodyMedium) }
                                }
                            }
                        }
                    }
                }
            }

            TextButton(onClick = { mainViewModel.navigateTo(Screen.Registro) }, enabled = !autenticando) {
                Text("¿No tienes cuenta? Crear una")
            }
        }
    }
}

@Composable
private fun TarjetaRol(rol: Rol, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoDe(rol), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rol.etiqueta, style = MaterialTheme.typography.titleMedium)
                Text(rol.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
