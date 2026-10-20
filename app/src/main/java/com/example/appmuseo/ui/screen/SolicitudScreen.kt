package com.example.appmuseo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.theme.AzulNoche
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

// Paso 2 de la compra: datos de contacto y direccion
@Composable
fun SolicitudScreen(
    solicitudViewModel: SolicitudViewModel,
    carritoViewModel: CarritoViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val formulario by solicitudViewModel.formulario.collectAsState()
    val items by carritoViewModel.items.collectAsState()
    val envio by carritoViewModel.envio.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val resumen = remember(items, envio) { carritoViewModel.resumen() }

    LaunchedEffect(Unit) {
        solicitudViewModel.precargarDatos(usuario.nombre, usuario.correo)
    }
    LaunchedEffect(resumen.cotizacion) {
        solicitudViewModel.usarEnvio(resumen.cotizacion)
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Datos de la compra", onVolver = { mainViewModel.navigateBack() }) }
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
            PasosCompra(pasoActual = 2)

            Text("Datos de contacto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CampoTexto(
                valor = formulario.nombre,
                onValorChange = solicitudViewModel::onNombreChange,
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = formulario.errores.nombre,
                esValido = Validaciones.validarNombre(formulario.nombre) == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = formulario.correo,
                onValorChange = solicitudViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = formulario.errores.correo,
                esValido = Validaciones.validarCorreo(formulario.correo) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            if (resumen.requiereEnvio) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Despacho", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                resumen.cotizacion?.let { cotizacion ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = AzulNoche)
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(cotizacion.zona.nombre, style = MaterialTheme.typography.titleSmall)
                                Text("${cotizacion.tipoEnvio.etiqueta} · ${cotizacion.plazo}", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { mainViewModel.navigateBack() }) { Text("Cambiar") }
                        }
                    }
                }
                CampoTexto(
                    valor = formulario.direccion,
                    onValorChange = solicitudViewModel::onDireccionChange,
                    etiqueta = "Dirección de despacho",
                    icono = Icons.Filled.Home,
                    error = formulario.errores.direccion ?: formulario.errores.zona,
                    esValido = formulario.direccion.isNotBlank(),
                    placeholder = "Calle, número, comuna"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = formulario.aceptaTerminos,
                        onValueChange = solicitudViewModel::onAceptaTerminosChange,
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = formulario.aceptaTerminos, onCheckedChange = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acepto los términos y condiciones (datos simulados)")
            }
            formulario.errores.terminos?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total a pagar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(formatearPrecio(resumen.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DoradoBoton)
            }
            Button(
                onClick = {
                    if (solicitudViewModel.validarFormulario(resumen.requiereEnvio)) {
                        mainViewModel.navigateTo(Screen.Resumen)
                    }
                },
                enabled = items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Revisar resumen de compra")
            }
        }
    }
}
