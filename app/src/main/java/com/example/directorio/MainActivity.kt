package com.example.directorio

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.navArgument
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.navigation.compose.*
import com.example.directorio.data.*
import com.example.directorio.views.ContactoViewModel
import com.example.directorio.views.ContactoViewModelF
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream

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
        WindowCompat.setDecorFitsSystemWindows(window, false) //Esto solo es una configuración para fullscreen.
        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            MaterialTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "lista",
                        modifier = Modifier.padding(paddingValues)
                    ) {

                        composable("lista") {
                            PantallaPrincipal(
                                viewModel = viewModel,
                                onAgregar = { navController.navigate("formulario/-1") },
                                onEditar = { id -> navController.navigate("formulario/$id") },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        composable(
                            "formulario/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val contactoId = backStackEntry.arguments?.getInt("id") ?: -1
                            var snackbarMessage by remember { mutableStateOf<String?>(null) }

                            if (snackbarMessage != null) {
                                LaunchedEffect(snackbarMessage) {
                                    navController.popBackStack()
                                    snackbarHostState.showSnackbar(snackbarMessage!!)
                                    snackbarMessage = null
                                }
                            }

                            FormularioContacto(
                                contactoId = contactoId,
                                viewModel = viewModel,
                                onGuardar = { mensaje -> snackbarMessage = mensaje }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PantallaPrincipal(
    viewModel: ContactoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchQuery by remember { mutableStateOf("") } //Variable para implementar una barra de búsqueda.
    val contactos by viewModel.todosLosContactos.observeAsState(emptyList())
    val contactosFiltrados = contactos.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.apellidoPaterno.contains(searchQuery, ignoreCase = true) ||
                it.apellidoMaterno.contains(searchQuery, ignoreCase = true) ||
                it.telefono.contains(searchQuery)
    }.sortedBy { it.nombre.lowercase() } //Aquí filtramos de varias maneras (alfabeticamente y por búsqueda)

    var contactoEliminadoId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Alto suficiente para imagen y searchbar
            ) {
                // Imagen de fondo.
                Image(
                    painter = painterResource(id = R.drawable.contacto_back),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding() + 15.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Text(
                        text = "Contactos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(65.dp)) //Espaciado para la barra de búsqueda.

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar contacto...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White.copy(alpha = 0.5f) //Transparencia.
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .shadow(4.dp) //Sombreado abajo de la searchbar
                            .clip(RoundedCornerShape(20.dp)) //Esquinas redondeadas.
                    )
                }
            }
        }
        ,
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        if (contactoEliminadoId != null) {
            LaunchedEffect(contactoEliminadoId) {
                snackbarHostState.showSnackbar("Contacto eliminado")
                contactoEliminadoId = null
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(contactosFiltrados, key = { it.id }) { contacto ->
                val dismissState = rememberDismissState() //Swipe.

                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    LaunchedEffect(contacto.id) {
                        viewModel.eliminar(contacto)
                        contactoEliminadoId = contacto.id
                    }
                }

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val color = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd, DismissDirection.EndToStart -> Color.Red
                            null -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    },
                    dismissContent = {
                        ContactoItem(
                            contacto = contacto,
                            onDelete = {}, // ya se maneja por swipe
                            onEdit = { onEditar(contacto.id) }
                        )
                    }
                )
            }

        }
    }
}

@Composable
fun ContactoItem(contacto: Contacto, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (!contacto.fotoUri.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(File(contacto.fotoUri)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${contacto.nombre} ${contacto.apellidoPaterno} ${contacto.apellidoMaterno}")
                Text(text = "📞 ${contacto.telefono}")
                Text(text = "✉️ ${contacto.correo}")
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text("Editar")
                    }
                }
            }
        }
    }
}

fun guardarImagenEnInterno(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val nombreArchivo = "img_${System.currentTimeMillis()}.jpg"
        val archivoDestino = File(context.filesDir, nombreArchivo)
        val outputStream = FileOutputStream(archivoDestino)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        archivoDestino.absolutePath // Devuelve la ruta del archivo.
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContacto(
    contactoId: Int,
    viewModel: ContactoViewModel,
    onGuardar: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contactoEditando by remember { mutableStateOf<Contacto?>(null) }
    val listaContactos by viewModel.todosLosContactos.observeAsState()

    val context = LocalContext.current

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagenGuardada by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            // Guardamos la imagen al almacenamiento interno.
            imagenGuardada = guardarImagenEnInterno(context, it)
        }
    }

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
                imagenUri = it.fotoUri?.let { uri -> Uri.parse(uri) }
            }
        }
    }

    val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    val telefonoValido = telefono.all { it.isDigit() } && telefono.length in 7..15

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (contactoEditando != null) "Editar contacto"
                        else "Agregar contacto"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onGuardar("Acción cancelada") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )

        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imagenUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imagenUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        tint = Color.Gray
                    )
                }
            }

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Seleccionar foto")
            }

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
                            correo = correo,
                            fotoUri = imagenGuardada ?: contactoEditando?.fotoUri // Usa la ruta interna real si hay nueva imagen, o mantiene la anterior
                        )
                        if (contactoEditando != null) {
                            viewModel.actualizar(nuevo)
                            onGuardar("Contacto actualizado")
                        } else {
                            viewModel.insertar(nuevo)
                            onGuardar("Contacto guardado")
                        }
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

