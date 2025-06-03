package com.example.directorio

//noinspection UsingMaterialAndMaterial3Libraries
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.directorio.data.*
import com.example.directorio.views.ContactoViewModel
import com.example.directorio.views.ContactoViewModelF
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val view = LocalView.current

            val esquemaColores = lightColorScheme(
                background = Color(0xFFD5C9DE),
                surface = Color(0xFFD5C9DE)
            )

            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
            }

            MaterialTheme(colorScheme = esquemaColores) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.systemBars
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(paddingValues)
                    ) {

                        composable("splash") {
                            SplashScreen(onFinish = {
                                navController.navigate("onboarding") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }

                        composable("onboarding") {
                            OnboardingScreen {
                                navController.navigate("lista") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }

                        composable("lista") {
                            PantallaPrincipal(
                                viewModel = viewModel,
                                onAgregar = { navController.navigate("formulario/-1") },
                                onEditar = { id -> navController.navigate("formulario/$id") },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        composable(
                            route = "formulario/{id}",
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

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash.json"))
    var isAnimationDone by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.5f,
        restartOnPlay = false
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && !isAnimationDone) {
            isAnimationDone = true
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF856C94)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(300.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    val slides = listOf(
        OnboardingSlide(
            title = "Bienvenido a\nDirectorio Telefónico",
            text = "Gestiona tus contactos de forma rápida y sencilla.",
            image = R.drawable.ic_onboarding_1
        ),
        OnboardingSlide(
            title = "Busca y Filtra",
            text = "Encuentra contactos rápidamente con nuestro buscador.",
            image = R.drawable.ic_onboarding_2
        ),
        OnboardingSlide(
            title = "Agrega Fotos",
            text = "Personaliza tus contactos con fotos.",
            image = R.drawable.ic_onboarding_3
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDE7F6))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val slide = slides[page]
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600),
                label = "AlphaAnimation"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(animatedAlpha)
            ) {
                Text(
                    text = slide.title,
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Image(
                    painter = painterResource(id = slide.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = slide.text,
                    style = TextStyle(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (page == slides.lastIndex) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onFinish) {
                        Text("Comenzar")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(slides.size) { index ->
                val color = if (pagerState.currentPage == index)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val text: String,
    val image: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PantallaPrincipal(
    viewModel: ContactoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var searchQuery by remember { mutableStateOf("") }
    val contactos by viewModel.todosLosContactos.observeAsState(emptyList())
    val contactosFiltrados = contactos.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.apellidoPaterno.contains(searchQuery, ignoreCase = true) ||
                it.apellidoMaterno.contains(searchQuery, ignoreCase = true) ||
                it.telefono.contains(searchQuery)
    }.sortedBy { it.nombre.lowercase() }

    var contactoEliminadoId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Fondo
                Image(
                    painter = painterResource(id = R.drawable.contacto_back),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Text(
                        text = "Contactos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar contacto...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        contentWindowInsets = WindowInsets.systemBars
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
                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { 150f },
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.eliminar(contacto)
                            contactoEliminadoId = contacto.id
                        }
                        true
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                            Color.Red
                        } else {
                            Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    },
                    content = {
                        ContactoItem(
                            contacto = contacto,
                            onDelete = {},
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
        archivoDestino.absolutePath
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Seleccionar foto")
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = paterno,
                onValueChange = { paterno = it },
                label = { Text("Apellido paterno") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = materno,
                onValueChange = { materno = it },
                label = { Text("Apellido materno") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                            fotoUri = imagenGuardada ?: contactoEditando?.fotoUri
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = correoValido && telefonoValido
            ) {
                Text(
                    if (contactoEditando != null) "Actualizar contacto"
                    else "Guardar contacto"
                )
            }
        }
    }
}
