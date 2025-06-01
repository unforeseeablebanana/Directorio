package com.example.directorio.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ContactoD {
    @Query("SELECT * FROM contactos") //Select desde la tabla de contactos.
    fun getAll(): LiveData<List<Contacto>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contacto: Contacto)

    @Update
    suspend fun update(contacto: Contacto)

    @Delete
    suspend fun delete(contacto: Contacto)
}
