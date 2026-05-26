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
fun FeedScreen(navController: NavController) {
    val dummyPosts = listOf(
        DogPost("1", "Rocco", "Golden Retriever", "Palermo, CABA", "Hace 2h", "https://images.unsplash.com/photo-1552053831-71594a27632d?q=80&w=1000", "Perdido"),
        DogPost("2", "Luna", "Border Collie", "Córdoba, Arg", "Hace 5h", "https://images.unsplash.com/photo-1507146426996-ef05306b995a?q=80&w=1000", "Encontrado"),
        DogPost("3", "Milo", "Pug", "Rosario, SF", "Ayer", "https://images.unsplash.com/photo-1517849845537-4d257902454a?q=80&w=1000", "Perdido")
    )

    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // BOX PRINCIPAL: Funciona como un eje Z para apilar capas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        // --- CAPA 1: EL FEED (Al fondo, ocupa toda la pantalla) ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // El bottom padding de 120.dp asegura que el último perrito se pueda scrollear
            // por encima de la barra para verlo completo.
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HEADER
            item {
                Column(modifier = Modifier.statusBarsPadding()) {
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
                        IconButton(onClick = { /* Notificaciones */ }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // BOTÓN DE FILTROS
                    Surface(
                        onClick = { showFilters = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Filtrar por raza, zona o estado...", color = Color.Gray, fontSize = 14.sp)
                            }
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Cerca de tu ubicación", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // LISTA DE MASCOTAS (ACÁ SE INYECTA LA NAVEGACIÓN)
            items(dummyPosts) { post ->
                DogImmersiveCard(
                    post = post,
                    onClick = {
                        // Viaja directo a la pantalla de detalle que armaste antes
                        navController.navigate(Screen.Detail.route)
                    }
                )
            }
        }

        // --- CAPA 2: NAVBAR BURBUJA (Flotando arriba de las fotos) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp) // Separación desde el borde inferior del celu
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(280.dp) // Ancho pastilla
                    .height(64.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color.White // Único fondo blanco, acotado a la pastilla
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Inicio
                    IconButton(onClick = { /* Ya estamos acá */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Inicio",
                            modifier = Modifier.size(26.dp),
                            tint = Color(0xFFFF9800)
                        )
                    }

                    // Agregar
                    IconButton(onClick = { navController.navigate(Screen.AISearch.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Reportar",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF8E8E93)
                        )
                    }

                    // Perfil
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(26.dp),
                            tint = Color(0xFF8E8E93)
                        )
                    }
                }
            }
        }

        // --- CAPA 3: MODAL DE FILTROS ---
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

@Composable
fun FilterContent(onApply: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)
    ) {
        Text("Filtros de búsqueda", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Estado", fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("Todos") })
            FilterChip(selected = false, onClick = {}, label = { Text("Perdidos") })
            FilterChip(selected = false, onClick = {}, label = { Text("Encontrados") })
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Raza", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = "", onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            placeholder = { Text("Escribí una raza...") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Aplicar Filtros", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ACÁ TAMBIÉN SE INYECTA LA NAVEGACIÓN EN EL COMPONENTE
@Composable
fun DogImmersiveCard(post: DogPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clickable { onClick() }, // Se hace clicable toda la tarjeta
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