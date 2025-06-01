package com.example.directorio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
                ContactoPantalla(viewModel)
            }
        }
    }
}

@Composable
fun ContactoPantalla(viewModel: ContactoViewModel) {
    var nombre by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var editando by remember { mutableStateOf<Contacto?>(null) }

    val contactos by viewModel.todosLosContactos.observeAsState(emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(if (editando == null) "Agregar nuevo contacto" else "Editar contacto",
            style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = paterno,
            onValueChange = { paterno = it },
            label = { Text("Apellido paterno") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = materno,
            onValueChange = { materno = it },
            label = { Text("Apellido materno") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone) //Aqui aseguramos que solo admita telefono.
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email) //Aqui aseguramos que solo admita correo.
        )

        val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
        val telefonoValido = telefono.all { it.isDigit() } && telefono.length in 7..15

        Button(
            onClick = {
                if (nombre.isNotBlank() && correoValido && telefonoValido) {
                    val contacto = Contacto(
                        id = editando?.id ?: 0,
                        nombre = nombre,
                        apellidoPaterno = paterno,
                        apellidoMaterno = materno,
                        telefono = telefono,
                        correo = correo
                    )
                    if (editando == null) viewModel.insertar(contacto)
                    else viewModel.actualizar(contacto)

                    nombre = ""; paterno = ""; materno = ""; telefono = ""; correo = ""; editando = null
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = correoValido && telefonoValido
        ) {
            Text(if (editando == null) "Guardar contacto" else "Actualizar contacto")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn {
            items(items = contactos, key = { it.id }) { contacto: Contacto ->
                ContactoItem(
                    contacto = contacto,
                    onDelete = { viewModel.eliminar(contacto) },
                    onEdit = {
                        nombre = contacto.nombre
                        paterno = contacto.apellidoPaterno
                        materno = contacto.apellidoMaterno
                        telefono = contacto.telefono
                        correo = contacto.correo
                        editando = contacto
                    }
                )
            }

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
