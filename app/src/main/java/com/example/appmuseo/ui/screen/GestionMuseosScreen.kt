package com.example.appmuseo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmuseo.data.remote.dto.MuseoDto
import com.example.appmuseo.navigation.Screen
import com.example.appmuseo.ui.components.AvisoRolJwt
import com.example.appmuseo.ui.components.BarraSuperior
import com.example.appmuseo.ui.components.DialogoConfirmacion
import com.example.appmuseo.ui.components.MensajeGestion
import com.example.appmuseo.ui.components.TarjetaGestion
import com.example.appmuseo.ui.theme.DoradoBoton
import com.example.appmuseo.viewmodel.GestionViewModel
import com.example.appmuseo.viewmodel.MainViewModel

// Pantalla 6 del caso: el Investigador registra y corrige fichas museológicas;
// el Administrador las revisa y puede retirar una ficha publicada
@Composable
fun GestionMuseosScreen(
    gestionViewModel: GestionViewModel,
    mainViewModel: MainViewModel,
    esAdministrador: Boolean,
    onCambios: () -> Unit = {}
) {
    val estado by gestionViewModel.estado.collectAsState()
    var porEliminar by remember { mutableStateOf<MuseoDto?>(null) }

    LaunchedEffect(Unit) { gestionViewModel.cargarMuseos() }

    porEliminar?.let { museo ->
        DialogoConfirmacion(
            titulo = "Retirar ficha",
            mensaje = "La ficha de «${museo.nombre}» dejará de aparecer en el mapa.",
            icono = Icons.Filled.Delete,
            textoConfirmar = "Retirar",
            esDestructiva = true,
            onConfirmar = {
                gestionViewModel.eliminarMuseo(museo)
                porEliminar = null
                onCambios()
            },
            onCancelar = { porEliminar = null }
        )
    }

    Scaffold(
        topBar = { BarraSuperior("Fichas de museos", onVolver = { mainViewModel.navigateBack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    gestionViewModel.nuevoMuseo()
                    mainViewModel.navigateTo(Screen.MuseoForm)
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva ficha") },
                containerColor = DoradoBoton
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (estado.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AvisoRolJwt(
                    if (esAdministrador) "Administrador · revisa, corrige o retira fichas publicadas"
                    else "Investigador · registra y corrige fichas museológicas"
                )
                MensajeGestion(estado.mensaje) { gestionViewModel.limpiarMensaje() }
                Text("${estado.museos.size} museos registrados", style = MaterialTheme.typography.labelLarge)
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.museos, key = { it.id }) { museo ->
                    TarjetaGestion(
                        icono = Icons.Filled.AccountBalance,
                        titulo = museo.nombre,
                        detalle = "${museo.ciudad}, ${museo.pais} · ${museo.tipo}",
                        etiqueta = "Levantamiento ${museo.anioLevantamiento}"
                    ) {
                        IconButton(onClick = {
                            gestionViewModel.editarMuseo(museo)
                            mainViewModel.navigateTo(Screen.MuseoForm)
                        }) { Icon(Icons.Filled.Edit, contentDescription = "Corregir ficha de ${museo.nombre}") }
                        // retirar una ficha publicada es decisión del Administrador
                        if (esAdministrador) {
                            IconButton(onClick = { porEliminar = museo }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Retirar ${museo.nombre}", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
