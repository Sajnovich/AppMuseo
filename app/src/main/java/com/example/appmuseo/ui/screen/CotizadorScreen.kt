package com.example.appmuseo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmuseo.utils.Validaciones
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.model.TipoDestino
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.utils.CotizadorEnvio
import com.example.appmuseo.utils.formatearPrecio
import com.example.appmuseo.viewmodel.CotizadorViewModel
import com.example.appmuseo.viewmodel.MainViewModel

@Composable
fun CotizadorScreen(cotizadorViewModel: CotizadorViewModel, mainViewModel: MainViewModel) {
    val estado by cotizadorViewModel.estado.collectAsState()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Cotizador de envíos", onVolver = { mainViewModel.navigateBack() }) }
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
            Text("Tipo de destino", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoDestino.entries.forEach { tipo ->
                    FilterChip(
                        selected = estado.tipoDestino == tipo,
                        onClick = { cotizadorViewModel.onTipoDestinoChange(tipo) },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }

            Text("Zona de destino", style = MaterialTheme.typography.titleMedium)
            CotizadorEnvio.zonasPorTipo(estado.tipoDestino).forEach { zona ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = estado.zona == zona,
                            onClick = { cotizadorViewModel.onZonaChange(zona) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.zona == zona, onClick = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(zona.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Base ${formatearPrecio(zona.tarifaBase)} + ${formatearPrecio(zona.costoPorKg)} por kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            estado.errores.zona?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            CampoTexto(
                valor = estado.peso,
                onValorChange = cotizadorViewModel::onPesoChange,
                etiqueta = "Peso del paquete (kg)",
                icono = Icons.Filled.Scale,
                placeholder = "Ej: 1,5",
                error = estado.errores.peso,
                esValido = Validaciones.validarPeso(estado.peso) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.montoCompra,
                onValorChange = cotizadorViewModel::onMontoCompraChange,
                etiqueta = "Monto de la compra (opcional)",
                icono = Icons.Filled.Payments,
                ayuda = "Envío nacional gratis desde ${formatearPrecio(CotizadorEnvio.MONTO_ENVIO_GRATIS)}",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { cotizadorViewModel.calcular() })
            )

            Button(
                onClick = { cotizadorViewModel.calcular() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Calcular envío")
            }

            val resultado = estado.resultado
            AnimatedVisibility(visible = resultado != null, enter = expandVertically() + fadeIn()) {
                if (resultado != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalShipping, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(resultado.zona.nombre, style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                if (resultado.envioGratis) "¡Envío gratis!" else formatearPrecio(resultado.costoEnvio),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Peso facturable: ${resultado.pesoFacturable} kg", style = MaterialTheme.typography.bodyMedium)
                            Text("Plazo estimado: ${resultado.plazo}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("¿Cómo se calcula?", style = MaterialTheme.typography.titleSmall)
                    Text("• Se cobra por kilo completo (mínimo 1 kg).", style = MaterialTheme.typography.bodySmall)
                    Text("• Costo = tarifa base + costo por kg × peso.", style = MaterialTheme.typography.bodySmall)
                    Text("• Máximo ${CotizadorEnvio.PESO_MAXIMO_KG.toInt()} kg por envío.", style = MaterialTheme.typography.bodySmall)
                    Text("• Valores simulados con fines académicos.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
