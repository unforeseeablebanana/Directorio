package com.example.directorio.data

import androidx.lifecycle.LiveData

class ContactoR(private val contactoD: ContactoD) {

    /*
      Este repositorio es la capa intermedia entre el DAO y el ViewModel.
      Sirve para que el ViewModel no interactúe directamente con ROOM,
      respetando el principio de separación de responsabilidades.
    */

    val todosLosContactos: LiveData<List<Contacto>> = contactoD.getAll()

    suspend fun insertar(contacto: Contacto) {
        contactoD.insert(contacto)
    }

    suspend fun actualizar(contacto: Contacto) {
        contactoD.update(contacto)
    }

    suspend fun eliminar(contacto: Contacto) {
        contactoD.delete(contacto)
    }
}
