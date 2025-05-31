package com.example.directorio.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Contacto::class], version = 1, exportSchema = false)
abstract class ADatabase : RoomDatabase() {

    abstract fun contactoDao(): ContactoDao

    companion object {
        @Volatile
        private var INSTANCE: ADatabase? = null

        fun getDatabase(context: Context): ADatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ADatabase::class.java,
                    "directorio_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
