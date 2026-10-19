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

/**
 * RECURSO NATIVO: NOTIFICACIONES LOCALES del sistema.
 * Aparecen en la barra de estado aunque la app esté en segundo plano.
 * Son "locales" porque las crea el mismo teléfono; las push reales (enviadas
 * desde un servidor con Firebase) quedan para cuando exista el backend.
 */
object Notificaciones {

    private const val CANAL_ID = "solicitudes"

    /**
     * Desde Android 8 toda notificación pertenece a un CANAL, que el usuario puede
     * silenciar desde Ajustes. Se crea al abrir la app (MainActivity); crearlo de nuevo no hace nada.
     */
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

    /** Desde Android 13 (API 33) mostrar notificaciones es un permiso que se pide en tiempo de ejecución. */
    fun necesitaPermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

    /**
     * Publica una notificación. Si el usuario no dio permiso, no se intenta
     * (la app sigue funcionando y el aviso se ve igual dentro de la app).
     *
     * @param id identificador: con el mismo id, la notificación nueva reemplaza a la anterior.
     */
    fun mostrar(context: Context, id: Int, titulo: String, texto: String) {
        if (necesitaPermiso(context)) return

        // Al tocar la notificación se abre la app (igual que tocar su ícono)
        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)   // desaparece al tocarla
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (e: SecurityException) {
            // El usuario quitó el permiso desde Ajustes justo ahora: no hacemos nada
        }
    }
}
