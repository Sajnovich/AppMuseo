package com.example.appmuseo.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appmuseo.R

object Notificaciones {

    private const val CANAL_ID = "solicitudes"

    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Solicitudes",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando una solicitud se registra o se completa"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun necesitaPermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

    fun mostrar(context: Context, id: Int, titulo: String, texto: String) {
        if (necesitaPermiso(context)) return

        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (e: SecurityException) {
        }
    }
}
