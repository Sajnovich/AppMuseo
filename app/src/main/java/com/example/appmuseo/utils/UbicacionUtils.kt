package com.example.appmuseo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/**
 * RECURSO NATIVO: UBICACIÓN del teléfono (GPS / red).
 *
 * Usamos la "última ubicación conocida" porque es instantánea y no gasta batería
 * (no enciende el GPS). Pedimos solo ubicación APROXIMADA (ACCESS_COARSE_LOCATION):
 * para saber qué museo queda más cerca no hace falta precisión de metros, y así
 * protegemos la privacidad del usuario (principio de "mínimo privilegio").
 *
 * @return la ubicación, o null si no hay permiso, la ubicación está apagada o el
 * teléfono aún no tiene una lectura guardada (pasa seguido en el emulador).
 */
fun obtenerUltimaUbicacion(context: Context): Location? {
    // Seguridad: nunca se lee la ubicación sin revisar antes el permiso
    val tienePermiso = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!tienePermiso) return null

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Revisamos cada proveedor activo (red, gps...) y nos quedamos con la lectura más reciente
    return manager.getProviders(true)
        .mapNotNull { proveedor ->
            try {
                manager.getLastKnownLocation(proveedor)
            } catch (e: SecurityException) {
                null   // ese proveedor exige un permiso que no pedimos → lo saltamos
            }
        }
        .maxByOrNull { it.time }
}
