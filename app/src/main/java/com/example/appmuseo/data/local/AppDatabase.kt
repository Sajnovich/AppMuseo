package com.example.appmuseo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ROOM - Base de datos: une las entidades (tablas) con sus DAOs.
 * Si en el futuro cambias SolicitudEntity, debes subir "version".
 */
@Database(entities = [SolicitudEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao

    companion object {
        // Patrón Singleton: una sola instancia de la BD para toda la app
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtenerBaseDeDatos(context: Context): AppDatabase {
            // Si ya existe la devolvemos; si no, la creamos. synchronized deja pasar un hilo a la vez
            // y adentro se vuelve a revisar, por si otro hilo la creó mientras este esperaba.
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "museo_db"            // nombre del archivo SQLite en el teléfono
                ).build().also { instancia = it }
            }
        }
    }
}
