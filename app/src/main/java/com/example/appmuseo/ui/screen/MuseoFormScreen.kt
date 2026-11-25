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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appmuseo.model.FormMuseo
import com.example.appmuseo.ui.components.AvisoRolJwt
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.CampoTexto
import com.example.appmuseo.ui.components.ChipsOpciones
import com.example.appmuseo.ui.components.EditorImagen
import com.example.appmuseo.utils.claveMuseo
import com.example.appmuseo.ui.components.MensajeGestion
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.viewmodel.GestionViewModel
import com.example.appmuseo.viewmodel.MainViewModel

// Formulario para registrar o corregir una ficha museológica (Investigador / Administrador)
@Composable
fun MuseoFormScreen(
    gestionViewModel: GestionViewModel,
    mainViewModel: MainViewModel,
    onCambios: () -> Unit = {}
) {
    val estado by gestionViewModel.estado.collectAsState()
    val form = estado.formMuseo
    val errores = estado.errores
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    val numerico = KeyboardOptions(keyboardType = KeyboardType.Number)

    Scaffold(
        topBar = { BarraSuperior(if (form.esNuevo) "Nueva ficha de museo" else "Corregir ficha", onVolver = { mainViewModel.navigateBack() }) }
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
            AvisoRolJwt(if (form.esNuevo) "Se registra con POST /api/museos" else "Se corrige con PUT /api/museos/${form.id}")

            EditorImagen(
                clave = form.id?.let { claveMuseo(it) },
                porDefecto = 0,
                cambio = form.imagen
            ) { cambio -> gestionViewModel.onMuseoChange("imagen") { it.copy(imagen = cambio) } }

            CampoTexto(form.nombre, { v -> gestionViewModel.onMuseoChange("nombre") { it.copy(nombre = v) } },
                "Nombre del museo", Icons.Filled.AccountBalance, error = errores["nombre"])
            Text("Tipo", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            ChipsOpciones(FormMuseo.TIPOS, form.tipo, { it }) { elegido ->
                gestionViewModel.onMuseoChange("tipo") { it.copy(tipo = elegido) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(form.pais, { v -> gestionViewModel.onMuseoChange("pais") { it.copy(pais = v) } },
                    "País", Icons.Filled.Public, error = errores["pais"], modifier = Modifier.weight(1f))
                CampoTexto(form.ciudad, { v -> gestionViewModel.onMuseoChange("ciudad") { it.copy(ciudad = v) } },
                    "Ciudad", Icons.Filled.LocationCity, error = errores["ciudad"], modifier = Modifier.weight(1f))
            }
            CampoTexto(form.descripcion, { v -> gestionViewModel.onMuseoChange("descripcion") { it.copy(descripcion = v) } },
                "Descripción breve", Icons.AutoMirrored.Filled.Notes, error = errores["descripcion"], singleLine = false)
            CampoTexto(form.detalle, { v -> gestionViewModel.onMuseoChange("detalle") { it.copy(detalle = v) } },
                "Detalle del recorrido virtual (opcional)", Icons.AutoMirrored.Filled.Notes, singleLine = false)

            Text("Ubicación en el mapa", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(form.latitud, { v -> gestionViewModel.onMuseoChange("latitud") { it.copy(latitud = v) } },
                    "Latitud", Icons.Filled.MyLocation, error = errores["latitud"], keyboardOptions = decimal, modifier = Modifier.weight(1f))
                CampoTexto(form.longitud, { v -> gestionViewModel.onMuseoChange("longitud") { it.copy(longitud = v) } },
                    "Longitud", Icons.Filled.Map, error = errores["longitud"], keyboardOptions = decimal, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoTexto(form.anio, { v -> gestionViewModel.onMuseoChange("anio") { it.copy(anio = v.filter(Char::isDigit)) } },
                    "Año", Icons.Filled.CalendarMonth, error = errores["anio"], keyboardOptions = numerico, modifier = Modifier.weight(1f))
                CampoTexto(form.calificacion, { v -> gestionViewModel.onMuseoChange("calificacion") { it.copy(calificacion = v) } },
                    "Nota", Icons.Filled.Star, error = errores["calificacion"], keyboardOptions = decimal, modifier = Modifier.weight(1f))
            }
            CampoTexto(form.resenas, { v -> gestionViewModel.onMuseoChange("resenas") { it.copy(resenas = v.filter(Char::isDigit)) } },
                "Reseñas", Icons.Filled.Reviews, error = errores["resenas"], keyboardOptions = numerico)

            MensajeGestion(estado.mensaje) { gestionViewModel.limpiarMensaje() }

            Button(
                onClick = {
                    gestionViewModel.guardarMuseo {
                        onCambios()
                        mainViewModel.navigateBack()
                    }
                },
                enabled = !estado.guardando,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoBoton),
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 4.dp)
            ) {
                if (estado.guardando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                else Text(if (form.esNuevo) "Registrar ficha" else "Guardar corrección")
            }
        }
    }
}
