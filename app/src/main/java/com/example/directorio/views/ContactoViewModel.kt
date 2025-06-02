package com.example.directorio.views

import androidx.lifecycle.*
import com.example.directorio.data.Contacto
import com.example.directorio.data.ContactoR
import kotlinx.coroutines.launch
import java.io.File

class ContactoViewModel(private val repository: ContactoR) : ViewModel() {

    /*
      Este ViewModel es el que se va a usar directamente desde las actividades
      o fragments para observar datos y hacer acciones como insertar, actualizar
      o eliminar.
    */

    val todosLosContactos: LiveData<List<Contacto>> = repository.todosLosContactos

    fun insertar(contacto: Contacto) = viewModelScope.launch {
        repository.insertar(contacto)
    }

    fun actualizar(contacto: Contacto) = viewModelScope.launch {
        repository.actualizar(contacto)
    }

    fun eliminar(contacto: Contacto) {
        viewModelScope.launch {
            // Borra la imagen guardada en almacenamiento interno.
            contacto.fotoUri?.let { ruta ->
                try {
                    val archivo = File(ruta)
                    if (archivo.exists()) archivo.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.eliminar(contacto)
        }
    }

}

// Factory para crear el ViewModel con parámetros.
class ContactoViewModelF(private val repository: ContactoR) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
