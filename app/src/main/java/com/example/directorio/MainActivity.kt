package com.example.directorio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.directorio.data.*
import com.example.directorio.views.ContactoViewModel
import com.example.directorio.views.ContactoViewModelF

class MainActivity : ComponentActivity() {

    private val viewModel: ContactoViewModel by viewModels {
        ContactoViewModelF(
            ContactoR(
                ADatabase.getDatabase(this).contactoDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaPrincipal(viewModel = viewModel, onAgregarClick = {
                    startActivity(Intent(this, FormularioActivity::class.java))
                })
            }
        }
    }
}

@Composable
fun PantallaPrincipal(viewModel: ContactoViewModel, onAgregarClick: () -> Unit) {
    val contactos by viewModel.todosLosContactos.observeAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(contactos, key = { it.id }) { contacto: Contacto ->
                ContactoItem(
                    contacto = contacto,
                    onDelete = { viewModel.eliminar(contacto) },
                    onEdit = { /* en esta vista aún no editamos */ }
                )
            }
        }

        FloatingActionButton(
            onClick = onAgregarClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar contacto")
        }
    }
}

@Composable
fun ContactoItem(contacto: Contacto, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${contacto.nombre} ${contacto.apellidoPaterno} ${contacto.apellidoMaterno}")
            Text(text = "📞 ${contacto.telefono}")
            Text(text = "✉️ ${contacto.correo}")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onEdit, modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    Text("Editar")
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text("Eliminar")
                }
            }
        }
    }
}

