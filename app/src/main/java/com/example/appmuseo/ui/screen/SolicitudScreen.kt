package com.example.appmuseo.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.Notificaciones
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CarritoViewModel
import com.example.appmuseo.viewmodel.MainViewModel
import com.example.appmuseo.viewmodel.SesionViewModel
import com.example.appmuseo.viewmodel.SolicitudViewModel

/**
 * FORMULARIO DE SOLICITUD de compra / inscripción (Guía 11):
 * CampoTexto (OutlinedTextField con isError/supportingText), RadioButton para la zona y Checkbox de términos.
 * Al confirmar, la solicitud se guarda en Room con estado PENDIENTE.
 */
@Composable
fun SolicitudScreen(
    solicitudViewModel: SolicitudViewModel,
    carritoViewModel: CarritoViewModel,
    sesionViewModel: SesionViewModel,
    mainViewModel: MainViewModel
) {
    val formulario by solicitudViewModel.formulario.collectAsState()
    val items by carritoViewModel.items.collectAsState()
    val usuario by sesionViewModel.usuario.collectAsState()
    val guardando by solicitudViewModel.guardando.collectAsState()
    val context = LocalContext.current

    // Guarda en Room; si sale bien, vacía el carrito y lleva a "Mis solicitudes"
    fun confirmar() {
        solicitudViewModel.confirmarSolicitud(items) {
            carritoViewModel.vaciar()
            mainViewModel.navigateTo(Screen.MisSolicitudes, popUpToRoute = Screen.Home)
        }
    }

    // El resultado del permiso no cambia el flujo: si lo niega, solo verá el aviso dentro de la app
    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> confirmar() }

    val subtotal = items.sumOf { it.subtotal }
    val pesoTotal = items.sumOf { it.producto.pesoKg * it.cantidad }
    val requiereEnvio = items.any { it.producto.requiereEnvio }
    val costoEnvio = if (requiereEnvio) formulario.cotizacion?.costoEnvio ?: 0 else 0

    // Al entrar, completamos nombre y correo con los datos de la sesión
    LaunchedEffect(Unit) {
        solicitudViewModel.precargarDatos(usuario.nombre, usuario.correo)
    }
    // Si cambian el peso o el subtotal del carrito, se recalcula el envío de la zona elegida
    LaunchedEffect(pesoTotal, subtotal) {
        solicitudViewModel.actualizarCotizacion(pesoTotal, subtotal)
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Solicitud", onVolver = { mainViewModel.navigateBack() }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()   // el teclado no tapa la dirección ni el botón
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Datos de contacto", style = MaterialTheme.typography.titleMedium)

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

            // Los datos de despacho solo aparecen si hay productos físicos
            if (requiereEnvio) {
                HorizontalDivider()
                Text("Despacho", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoDestino.entries.forEach { tipo ->
                        FilterChip(
                            selected = formulario.tipoDestino == tipo,
                            onClick = { solicitudViewModel.onTipoDestinoChange(tipo) },
                            label = { Text(tipo.etiqueta) }
                        )
                    }
                }

                // Lista de zonas con RadioButton. "selectable" hace clickeable toda la fila (más fácil de tocar)
                CotizadorEnvio.zonasPorTipo(formulario.tipoDestino).forEach { zona ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = formulario.zona == zona,
                                onClick = { solicitudViewModel.onZonaChange(zona, pesoTotal, subtotal) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = formulario.zona == zona, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(zona.nombre, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                formulario.errores.zona?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                CampoTexto(
                    valor = formulario.direccion,
                    onValorChange = solicitudViewModel::onDireccionChange,
                    etiqueta = "Dirección de despacho",
                    icono = Icons.Filled.Home,
                    error = formulario.errores.direccion,
                    esValido = formulario.direccion.isNotBlank(),
                    placeholder = "Calle, número, comuna"
                )
            }

            // Términos y condiciones: toda la fila es clickeable (accesibilidad)
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

            // Resumen de la solicitud
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    items.forEach { item ->
                        FilaResumen("${item.cantidad}x ${item.producto.nombre}", formatearPrecio(item.subtotal))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    FilaResumen("Subtotal", formatearPrecio(subtotal))
                    if (requiereEnvio) {
                        val cotizacion = formulario.cotizacion
                        FilaResumen(
                            "Envío",
                            when {
                                cotizacion == null -> "Elige una zona"
                                cotizacion.envioGratis -> "Gratis"
                                else -> "${formatearPrecio(cotizacion.costoEnvio)} · ${cotizacion.plazo}"
                            }
                        )
                    }
                    FilaResumen("Total", formatearPrecio(subtotal + costoEnvio), destacado = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // 1) Primero se valida: si hay errores, se marcan los campos y no se sigue
                    if (solicitudViewModel.validarFormulario(requiereEnvio)) {
                        // 2) Se pide el permiso de notificaciones en el momento en que tiene sentido
                        //    (Android 13+). Con o sin permiso, la solicitud se guarda igual.
                        if (Notificaciones.necesitaPermiso(context)) {
                            lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            confirmar()
                        }
                    }
                },
                enabled = items.isNotEmpty() && !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Confirmar solicitud")
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            etiqueta,
            modifier = Modifier.weight(1f),
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
    }
}
