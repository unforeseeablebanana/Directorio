package com.example.directorio

import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.navArgument
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.example.directorio.data.*
import com.example.directorio.views.ContactoViewModel
import com.example.directorio.views.ContactoViewModelF

@OptIn(ExperimentalMaterial3Api::class)
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
            val navController = rememberNavController()

            MaterialTheme {
                NavHost(navController = navController, startDestination = "lista") {

                    composable("lista") {
                        PantallaPrincipal(
                            viewModel = viewModel,
                            onAgregar = { navController.navigate("formulario/-1") },
                            onEditar = { id -> navController.navigate("formulario/$id") }
                        )
                    }

                    composable(
                        "formulario/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val contactoId = backStackEntry.arguments?.getInt("id") ?: -1
                        FormularioContacto(
                            contactoId = contactoId,
                            viewModel = viewModel,
                            onGuardar = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaPrincipal(
    viewModel: ContactoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val contactos by viewModel.todosLosContactos.observeAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            items(contactos, key = { it.id }) { contacto ->
                ContactoItem(
                    contacto = contacto,
                    onDelete = { viewModel.eliminar(contacto) },
                    onEdit = { onEditar(contacto.id) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContacto(
    contactoId: Int,
    viewModel: ContactoViewModel,
    onGuardar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contactoEditando by remember { mutableStateOf<Contacto?>(null) }

    val listaContactos by viewModel.todosLosContactos.observeAsState()

    LaunchedEffect(contactoId, listaContactos) {
        if (contactoId != -1 && contactoEditando == null) {
            val contacto = listaContactos?.find { it.id == contactoId }
            contacto?.let {
                nombre = it.nombre
                paterno = it.apellidoPaterno
                materno = it.apellidoMaterno
                telefono = it.telefono
                correo = it.correo
                contactoEditando = it
            }
        }
    }

    val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    val telefonoValido = telefono.all { it.isDigit() } && telefono.length in 7..15

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formulario") },
                navigationIcon = {
                    IconButton(onClick = onGuardar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

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
                        val nuevo = Contacto(
                            id = contactoEditando?.id ?: 0,
                            nombre = nombre,
                            apellidoPaterno = paterno,
                            apellidoMaterno = materno,
                            telefono = telefono,
                            correo = correo
                        )
                        if (contactoEditando != null) {
                            viewModel.actualizar(nuevo)
                        } else {
                            viewModel.insertar(nuevo)
                        }
                        onGuardar()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = correoValido && telefonoValido
            ) {
                Text(if (contactoEditando != null) "Actualizar contacto" else "Guardar contacto")
            }
        }
    }
}


