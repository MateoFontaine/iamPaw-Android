package com.example.iampaw.components.feed

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.iampaw.components.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = hiltViewModel()
) {
    val state by feedViewModel.uiState.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var feedSearchText by remember { mutableStateOf("") }

    // Filtro 100% local: escribir no toca Room ni el ViewModel → no pierde foco
    val filteredPosts = remember(state.posts, feedSearchText) {
        val query = feedSearchText.trim().lowercase()
        if (query.isEmpty()) {
            state.posts
        } else {
            state.posts.filter { post ->
                post.name.lowercase().contains(query) ||
                    post.breed.lowercase().contains(query) ||
                    post.location.lowercase().contains(query) ||
                    post.status.lowercase().contains(query)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        if (state.isLoading && state.posts.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFF9800)
            )
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "feed_header") {
                Column {
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
                        IconButton(onClick = { /* TODO: Notificaciones */ }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = feedSearchText,
                            onValueChange = { feedSearchText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar por nombre o raza") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (feedSearchText.isNotEmpty()) {
                                    IconButton(onClick = { feedSearchText = "" }) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Borrar")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Surface(
                            onClick = { showFilters = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFF3E0),
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

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Cerca de tu ubicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!state.isLoading && filteredPosts.isEmpty()) {
                item(key = "feed_empty") {
                    Text(
                        text = "No se encontraron mascotas",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(filteredPosts, key = { it.id }) { post ->
                DogImmersiveCard(
                    post = post,
                    onClick = { navController.navigate(Screen.Detail.route) }
                )
            }
        }

        // Navbar burbuja — único elemento fijo
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
