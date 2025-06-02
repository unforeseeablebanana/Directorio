package com.example.directorio.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos")
data class Contacto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val telefono: String,
    val correo: String,
    val fotoUri: String? = null // ← Nuevo campo opcional para la foto
)