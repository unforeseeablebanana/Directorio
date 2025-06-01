package com.example.directorio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.directorio.data.*
import com.example.directorio.views.ContactoViewModel
import com.example.directorio.views.ContactoViewModelF

@OptIn(ExperimentalMaterial3Api::class) //Usamos una API experimental de Material3, por lo que es necesario importar la anotación correspondiente.
class FormularioActivity : ComponentActivity() {

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
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Nuevo contacto") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        )
                    }
                ) { padding ->
                    FormularioContacto(
                        onGuardar = { finish() },
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun FormularioContacto(
    onGuardar: () -> Unit,
    viewModel: ContactoViewModel,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    val telefonoValido = telefono.all { it.isDigit() } && telefono.length in 7..15

    Column(modifier = modifier.padding(16.dp)) {
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Button(
            onClick = {
                if (nombre.isNotBlank() && correoValido && telefonoValido) {
                    val contacto = Contacto(
                        nombre = nombre,
                        apellidoPaterno = paterno,
                        apellidoMaterno = materno,
                        telefono = telefono,
                        correo = correo
                    )
                    viewModel.insertar(contacto)
                    onGuardar()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = correoValido && telefonoValido
        ) {
            Text("Guardar contacto")
        }
    }
}
