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
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.model.ResultadoRegistro
import com.example.appmuseo.model.TipoUsuario
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.viewmodel.LoginViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    registroViewModel: RegistroViewModel,
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel
) {
    val estado by registroViewModel.estado.collectAsState()
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val registrando = estado.resultado is ResultadoRegistro.Registrando

    // si la cuenta se creo
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

            CampoTexto(
                valor = estado.nombre,
                onValorChange = registroViewModel::onNombreChange,
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = estado.errores.nombre,
                esValido = Validaciones.validarNombre(estado.nombre) == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.correo,
                onValorChange = registroViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = estado.errores.correo,
                esValido = Validaciones.validarCorreo(estado.correo) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.clave,
                onValorChange = registroViewModel::onClaveChange,
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errores.clave,
                ayuda = "Mínimo 6 caracteres, con letras y números",
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                accionFinal = {
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            )
            CampoTexto(
                valor = estado.confirmarClave,
                onValorChange = registroViewModel::onConfirmarClaveChange,
                etiqueta = "Repetir contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errores.confirmarClave,
                esValido = estado.confirmarClave.isNotEmpty() &&
                        Validaciones.validarConfirmacion(estado.clave, estado.confirmarClave) == null,
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

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
