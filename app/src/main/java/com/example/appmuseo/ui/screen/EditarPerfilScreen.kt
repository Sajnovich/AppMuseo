package com.example.appmuseo.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.PerfilViewModel
import com.example.appmuseo.viewmodel.SesionViewModel

@Composable
fun EditarPerfilScreen(
    perfilViewModel: PerfilViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val estado by perfilViewModel.edicion.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val context = LocalContext.current
    var mostrarClaves by remember { mutableStateOf(false) }
    val transformacion = if (mostrarClaves) VisualTransformation.None else PasswordVisualTransformation()

    val ojo: @Composable () -> Unit = {
        IconButton(onClick = { mostrarClaves = !mostrarClaves }) {
            Icon(
                if (mostrarClaves) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (mostrarClaves) "Ocultar contraseñas" else "Mostrar contraseñas"
            )
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis datos personales", onVolver = { mainViewModel.navigateBack() }) }
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
            Text("Datos de la cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CampoTexto(
                valor = estado.nombre,
                onValorChange = perfilViewModel::onNombreChange,
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = estado.errores.nombre,
                esValido = Validaciones.validarNombre(estado.nombre) == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.correo,
                onValorChange = perfilViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = estado.errores.correo,
                esValido = Validaciones.validarCorreo(estado.correo) == null,
                ayuda = "Con este correo inicias sesión. Tu historial se mantiene.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            // interruptor para mostrar los campos de contraseña nueva
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LockReset, contentDescription = null, tint = AzulNoche)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Cambiar contraseña", style = MaterialTheme.typography.bodyLarge)
                        Text("Mínimo 6 caracteres, con letras y números", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = estado.cambiarClave,
                        onCheckedChange = perfilViewModel::onCambiarClaveChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = DoradoBoton)
                    )
                }
            }

            AnimatedVisibility(visible = estado.cambiarClave) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(
                        valor = estado.claveNueva,
                        onValorChange = perfilViewModel::onClaveNuevaChange,
                        etiqueta = "Contraseña nueva",
                        icono = Icons.Filled.Key,
                        error = estado.errores.claveNueva,
                        esValido = Validaciones.validarClaveSegura(estado.claveNueva) == null,
                        visualTransformation = transformacion,
                        accionFinal = ojo,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                    )
                    CampoTexto(
                        valor = estado.repetirClave,
                        onValorChange = perfilViewModel::onRepetirClaveChange,
                        etiqueta = "Repetir contraseña nueva",
                        icono = Icons.Filled.Key,
                        error = estado.errores.repetirClave,
                        esValido = estado.repetirClave.isNotEmpty() && estado.repetirClave == estado.claveNueva,
                        visualTransformation = transformacion,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Confirma que eres tú", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CampoTexto(
                valor = estado.claveActual,
                onValorChange = perfilViewModel::onClaveActualChange,
                etiqueta = "Contraseña actual",
                icono = Icons.Filled.Lock,
                error = estado.errores.claveActual,
                visualTransformation = transformacion,
                accionFinal = ojo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            estado.errorGeneral?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    perfilViewModel.guardarCambios(usuario.correo) { perfil ->
                        // la sesion se actualiza con los datos nuevos
                        sesionViewModel.iniciarSesion(perfil)
                        Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                        mainViewModel.navigateBack()
                    }
                },
                enabled = !estado.guardando,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Guardar cambios")
                }
            }
        }
    }
}
