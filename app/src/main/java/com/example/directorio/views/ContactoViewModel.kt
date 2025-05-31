package com.example.directorio.views

import androidx.lifecycle.*
import com.example.directorio.data.Contacto
import com.example.directorio.data.ContactoR
import kotlinx.coroutines.launch

class ContactoViewModel(private val repository: ContactoR) : ViewModel() {

    val todosLosContactos: LiveData<List<Contacto>> = repository.todosLosContactos

    fun insertar(contacto: Contacto) = viewModelScope.launch {
        repository.insertar(contacto)
    }

    fun actualizar(contacto: Contacto) = viewModelScope.launch {
        repository.actualizar(contacto)
    }

    fun eliminar(contacto: Contacto) = viewModelScope.launch {
        repository.eliminar(contacto)
    }
}

// Factory para crear el ViewModel con parámetros
class ContactoViewModelFactory(private val repository: ContactoR) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
