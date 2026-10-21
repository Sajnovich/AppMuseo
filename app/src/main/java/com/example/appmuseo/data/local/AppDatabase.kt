package com.example.appmuseo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SolicitudEntity::class, UsuarioEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        // patrón singleton
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtenerBaseDeDatos(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "museo_db"   // nombre del archivo sqlite en el teléfono
                )
                    // Con datos reales se escribiría una Migration para no
                    .fallbackToDestructiveMigration()
                    .build().also { instancia = it }
            }
        }
    }
}
