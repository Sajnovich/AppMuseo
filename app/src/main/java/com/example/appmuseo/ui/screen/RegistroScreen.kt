package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.model.TipoUsuario
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.RegistroViewModel

/**
 * CREAR CUENTA: formulario completo con validación por campo (Guía 11).
 * La cuenta se guarda en Room y luego se puede iniciar sesión con ella.
 */
@Composable
fun RegistroScreen(
    registroViewModel: RegistroViewModel,
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel
) {
    val estado by registroViewModel.estado.collectAsState()
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val registrando = estado.resultado is ResultadoRegistro.Registrando

    // Si la cuenta se creó: volvemos al login con el correo ya escrito
    LaunchedEffect(estado.resultado) {
        val resultado = estado.resultado
        if (resultado is ResultadoRegistro.Exito) {
            loginViewModel.prepararTrasRegistro(resultado.correo)
            registroViewModel.limpiarFormulario()
            mainViewModel.navigateBack()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Crear cuenta", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Tus datos", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = estado.nombre,
                onValueChange = registroViewModel::onNombreChange,
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = estado.errores.nombre != null,
                supportingText = { estado.errores.nombre?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.correo,
                onValueChange = registroViewModel::onCorreoChange,
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = estado.errores.correo != null,
                supportingText = { estado.errores.correo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.clave,
                onValueChange = registroViewModel::onClaveChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                isError = estado.errores.clave != null,
                supportingText = {
                    Text(
                        estado.errores.clave ?: "Mínimo 6 caracteres, con letras y números",
                        color = if (estado.errores.clave != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.confirmarClave,
                onValueChange = registroViewModel::onConfirmarClaveChange,
                label = { Text("Repetir contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                isError = estado.errores.confirmarClave != null,
                supportingText = { estado.errores.confirmarClave?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            // Perfil de usuario (los tres perfiles externos del caso)
            Text("¿Cuál es tu perfil?", style = MaterialTheme.typography.titleMedium)
            TipoUsuario.entries.forEach { tipo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = estado.tipoUsuario == tipo,
                            onClick = { registroViewModel.onTipoUsuarioChange(tipo) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.tipoUsuario == tipo, onClick = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tipo.etiqueta)
                }
            }
            estado.errores.tipoUsuario?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = estado.aceptaTerminos,
                        onValueChange = registroViewModel::onAceptaTerminosChange,
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = estado.aceptaTerminos, onCheckedChange = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acepto los términos y condiciones (datos simulados)")
            }
            estado.errores.terminos?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Error que devuelve el repositorio (ej: correo ya registrado)
            val resultado = estado.resultado
            AnimatedVisibility(visible = resultado is ResultadoRegistro.Error) {
                Text(
                    (resultado as? ResultadoRegistro.Error)?.mensaje ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { registroViewModel.registrar() },
                enabled = !registrando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (registrando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear cuenta")
                }
            }
        }
    }
}
