package com.example.appmuseo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.Categoria
import com.example.appmuseo.ui.components.AvisoRolJwt
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.components.ChipsOpciones
import com.example.appmuseo.ui.components.EditorImagen
import com.example.appmuseo.ui.components.imagenDeProducto
import com.example.appmuseo.utils.claveProducto
import com.example.appmuseo.ui.components.MensajeGestion
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.viewmodel.GestionViewModel
import com.example.appmuseo.viewmodel.MainViewModel

// Formulario de alta y modificación de un producto o curso (Administrador)
@Composable
fun ProductoFormScreen(
    gestionViewModel: GestionViewModel,
    mainViewModel: MainViewModel,
    onCambios: () -> Unit = {}
) {
    val estado by gestionViewModel.estado.collectAsState()
    val form = estado.formProducto
    val errores = estado.errores
    val numerico = KeyboardOptions(keyboardType = KeyboardType.Number)
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    Scaffold(
        topBar = { BarraSuperior(if (form.esNuevo) "Nuevo producto o curso" else "Editar producto", onVolver = { mainViewModel.navigateBack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AvisoRolJwt(if (form.esNuevo) "Se crea con POST /api/productos" else "Se actualiza con PUT /api/productos/${form.id}")
            Text("Categoría", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
            ChipsOpciones(Categoria.entries, form.categoria, { it.etiqueta }) { elegida ->
                gestionViewModel.onProductoChange("categoria") { it.copy(categoria = elegida) }
            }

            EditorImagen(
                clave = form.id?.let { claveProducto(it) },
                porDefecto = form.id?.let { imagenDeProducto(it.toInt()) } ?: 0,
                cambio = form.imagen
            ) { cambio -> gestionViewModel.onProductoChange("imagen") { it.copy(imagen = cambio) } }

            CampoTexto(form.nombre, { v -> gestionViewModel.onProductoChange("nombre") { it.copy(nombre = v) } },
                "Nombre", Icons.Filled.Sell, error = errores["nombre"])
            CampoTexto(form.descripcion, { v -> gestionViewModel.onProductoChange("descripcion") { it.copy(descripcion = v) } },
                "Descripción", Icons.AutoMirrored.Filled.Notes, error = errores["descripcion"], singleLine = false)
            CampoTexto(form.precio, { v -> gestionViewModel.onProductoChange("precio") { it.copy(precio = v.filter(Char::isDigit)) } },
                "Precio (CLP)", Icons.Filled.AttachMoney, error = errores["precio"], keyboardOptions = numerico)
            CampoTexto(form.stock, { v -> gestionViewModel.onProductoChange("stock") { it.copy(stock = v.filter(Char::isDigit)) } },
                if (form.esCurso) "Cupos disponibles" else "Stock", Icons.Filled.Inventory2, error = errores["stock"], keyboardOptions = numerico)

            if (form.esCurso) {
                CampoTexto(form.modalidad, { v -> gestionViewModel.onProductoChange("modalidad") { it.copy(modalidad = v) } },
                    "Modalidad (Online, Presencial...)", Icons.Filled.School, error = errores["modalidad"])
                CampoTexto(form.semanas, { v -> gestionViewModel.onProductoChange("semanas") { it.copy(semanas = v.filter(Char::isDigit)) } },
                    "Duración en semanas", Icons.Filled.CalendarMonth, error = errores["semanas"], keyboardOptions = numerico)
            } else {
                CampoTexto(form.pesoKg, { v -> gestionViewModel.onProductoChange("pesoKg") { it.copy(pesoKg = v) } },
                    "Peso (kg)", Icons.Filled.Scale, error = errores["pesoKg"], keyboardOptions = decimal, ayuda = "Se usa para calcular el envío")
                CampoTexto(form.codigoBarra, { v -> gestionViewModel.onProductoChange("codigoBarra") { it.copy(codigoBarra = v) } },
                    "Código QR / barra (opcional)", Icons.Filled.QrCode, error = errores["codigoBarra"], placeholder = "MDM-000")
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Mostrar en Destacados", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = form.destacado,
                    onCheckedChange = { v -> gestionViewModel.onProductoChange("destacado") { it.copy(destacado = v) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = DoradoBoton)
                )
            }

            MensajeGestion(estado.mensaje) { gestionViewModel.limpiarMensaje() }

            Button(
                onClick = {
                    gestionViewModel.guardarProducto {
                        onCambios()
                        mainViewModel.navigateBack()
                    }
                },
                enabled = !estado.guardando,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton),
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 4.dp)
            ) {
                if (estado.guardando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                else Text(if (form.esNuevo) "Agregar al catálogo" else "Guardar cambios")
            }
        }
    }
}
