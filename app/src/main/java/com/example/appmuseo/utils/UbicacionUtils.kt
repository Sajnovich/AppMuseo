package com.example.appmuseo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

fun obtenerUltimaUbicacion(context: Context): Location? {
    // Seguridad
    val tienePermiso = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!tienePermiso) return null

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return manager.getProviders(true)
        .mapNotNull { proveedor ->
            try {
                manager.getLastKnownLocation(proveedor)
            } catch (e: SecurityException) {
                null
            }
        }
        .maxByOrNull { it.time }
}
