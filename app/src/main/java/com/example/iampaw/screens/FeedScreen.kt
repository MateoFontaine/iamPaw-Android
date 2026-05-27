package com.example.iampaw.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.iampaw.components.Screen

// Modelo de datos
data class DogPost(
    val id: String,
    val name: String,
    val breed: String,
    val location: String,
    val time: String,
    val imageUrl: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = viewModel() // Traemos el ViewModel para tener las razas
) {
    val dummyPosts = listOf(
        DogPost("1", "Rocco", "Golden Retriever", "Palermo, CABA", "Hace 2h", "https://images.unsplash.com/photo-1552053831-71594a27632d?q=80&w=1000", "Perdido"),
        DogPost("2", "Luna", "Border Collie", "Córdoba, Arg", "Hace 5h", "https://images.unsplash.com/photo-1507146426996-ef05306b995a?q=80&w=1000", "Encontrado"),
        DogPost("3", "Milo", "Pug", "Rosario, SF", "Ayer", "https://images.unsplash.com/photo-1517849845537-4d257902454a?q=80&w=1000", "Perdido")
    )

    // Estados para la UI
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Estados para el buscador de razas
    val breeds by reportViewModel.breeds.collectAsState()
    var searchBreedText by remember { mutableStateOf("") }
    var expandedBreedSearch by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        // --- CAPA 1: EL FEED ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(modifier = Modifier.statusBarsPadding()) {
                    // HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color.Black)) { append("iam") }
                                withStyle(SpanStyle(color = Color(0xFFFF9800))) { append("Paw") }
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        // Campanita inactiva para la demo
                        IconButton(onClick = { /* TODO: Notificaciones */ }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // BARRA DE BÚSQUEDA Y FILTROS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Buscador Autocompletable (Ocupa el espacio principal)
                        ExposedDropdownMenuBox(
                            expanded = expandedBreedSearch,
                            onExpandedChange = { expandedBreedSearch = !expandedBreedSearch },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = searchBreedText,
                                onValueChange = {
                                    searchBreedText = it
                                    expandedBreedSearch = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                placeholder = { Text("Buscar raza...") },
                                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFFF9800)) },
                                trailingIcon = {
                                    if (searchBreedText.isNotEmpty()) {
                                        IconButton(onClick = { searchBreedText = "" }) {
                                            Icon(Icons.Outlined.Close, contentDescription = "Borrar", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9800),
                                    unfocusedBorderColor = Color(0xFFEEEEEE),
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )

                            // Menú desplegable con las razas filtradas
                            val filteredOptions = breeds.filter { it.name.contains(searchBreedText, ignoreCase = true) }
                            if (filteredOptions.isNotEmpty() && searchBreedText.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = expandedBreedSearch,
                                    onDismissRequest = { expandedBreedSearch = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    filteredOptions.take(4).forEach { breed ->
                                        DropdownMenuItem(
                                            text = { Text(breed.name, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                searchBreedText = breed.name
                                                expandedBreedSearch = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Botón de Filtros Avanzados (El de las rayitas)
                        Surface(
                            onClick = { showFilters = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFF3E0), // Fondo naranjita claro
                            border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                        ) {
                            Icon(
                                Icons.Outlined.Tune,
                                contentDescription = "Filtros",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Cerca de tu ubicación", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // LISTA DE MASCOTAS
            items(dummyPosts) { post ->
                DogImmersiveCard(
                    post = post,
                    onClick = { navController.navigate(Screen.Detail.route) }
                )
            }
        }

        // --- CAPA 2: NAVBAR BURBUJA ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .height(64.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Ya estamos acá */ }) {
                        Icon(Icons.Outlined.Home, contentDescription = "Inicio", modifier = Modifier.size(26.dp), tint = Color(0xFFFF9800))
                    }
                    IconButton(onClick = { navController.navigate(Screen.AISearch.route) }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Reportar", modifier = Modifier.size(28.dp), tint = Color(0xFF8E8E93))
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Perfil", modifier = Modifier.size(26.dp), tint = Color(0xFF8E8E93))
                    }
                }
            }
        }

        // --- CAPA 3: MODAL DE FILTROS RÁPIDOS ---
        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                FilterContent { showFilters = false }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterContent(onApply: () -> Unit) {
    // Estados para los chips (solo visuales para la demo)
    var selectedStatus by remember { mutableStateOf("Todos") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)
    ) {
        Text("Filtros rápidos", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Estado del reporte", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        Row(
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedStatus == "Todos",
                onClick = { selectedStatus = "Todos" },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFE0B2))
            )
            FilterChip(
                selected = selectedStatus == "Perdidos",
                onClick = { selectedStatus = "Perdidos" },
                label = { Text("Perdidos") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFE0B2))
            )
            FilterChip(
                selected = selectedStatus == "Encontrados",
                onClick = { selectedStatus = "Encontrados" },
                label = { Text("Encontrados") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFE0B2))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Aplicar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DogImmersiveCard(post: DogPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 500f
                    )
                )
            )
            Surface(
                color = if (post.status == "Perdido") Color(0xFFEF5350) else Color(0xFF66BB6A),
                modifier = Modifier.padding(20.dp).align(Alignment.TopEnd),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = post.status,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Text(post.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text("${post.breed} • ${post.time}", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(post.location, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}