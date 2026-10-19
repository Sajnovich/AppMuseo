package com.example.appmuseo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.appmuseo.model.Museo
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Centro inicial: Iberoamérica completa
private val PUNTO_INICIAL = GeoPoint(-5.0, -60.0)
private const val ZOOM_INICIAL = 2.5
private const val ZOOM_MUSEO = 11.0

/**
 * MAPA REAL de OpenStreetMap con osmdroid (mismo enfoque del ejemplo 103 del curso):
 *  - osmdroid es gratis y NO necesita API key.
 *  - El MapView es una View clásica: se muestra dentro de Compose con AndroidView.
 *  - Se conecta al ciclo de vida (onResume / onPause) con DisposableEffect.
 * Cada museo es un marcador; al tocarlo se selecciona y se abre su globo con el nombre.
 */
@Composable
fun MapaMuseosOsm(
    museos: List<Museo>,
    seleccionado: Museo?,
    onSeleccionar: (Museo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)   // estilo clásico de OpenStreetMap
            setMultiTouchControls(true)                // zoom con dos dedos
            isTilesScaledToDpi = true
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(ZOOM_INICIAL)
            controller.setCenter(PUNTO_INICIAL)
            // El mapa está dentro de una lista con scroll: al tocarlo, la lista no le "roba" el gesto
            setOnTouchListener { vista, _ ->
                vista.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    // Ciclo de vida: el mapa descarga baldosas solo mientras la pantalla está visible
    val cicloDeVida = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(cicloDeVida) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        cicloDeVida.addObserver(observador)
        onDispose {
            cicloDeVida.removeObserver(observador)
            mapView.onDetach()   // libera la memoria del mapa
        }
    }

    // Al elegir un museo (en la lista, el filtro o "más cercano"), el mapa vuela hasta él
    LaunchedEffect(seleccionado) {
        seleccionado?.let { mapView.controller.animateTo(GeoPoint(it.latitud, it.longitud), ZOOM_MUSEO, 800L) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        // update se ejecuta cada vez que cambia la lista (filtro por país) o el seleccionado
        update = { mapa ->
            mapa.overlays.removeAll { it is Marker }
            museos.forEach { museo ->
                val marcador = Marker(mapa).apply {
                    position = GeoPoint(museo.latitud, museo.longitud)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = museo.nombre
                    snippet = "${museo.ciudad}, ${museo.pais}"
                    setOnMarkerClickListener { m, _ ->
                        onSeleccionar(museo)
                        m.showInfoWindow()
                        true
                    }
                }
                mapa.overlays.add(marcador)
                if (museo.id == seleccionado?.id) marcador.showInfoWindow()
            }
            mapa.invalidate()   // redibuja el mapa con los marcadores nuevos
        }
    )
}
