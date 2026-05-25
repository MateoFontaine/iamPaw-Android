package com.example.iampaw.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val orangePaw = Color(0xFFFF9800)
    val bgColor = Color(0xFFFBFBFB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.Black)) { append("Mi ") }
                        withStyle(SpanStyle(color = orangePaw)) { append("Perfil") }
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                IconButton(onClick = { /* Configuración */ }) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- INFO DE USUARIO ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .border(2.dp, orangePaw, CircleShape)
                        .padding(4.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    AsyncImage(
                        model = user?.photoUrl ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y",
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "Usuario de iamPaw",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = user?.email ?: "Sin correo vinculado",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- ESTADÍSTICAS RÁPIDAS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), label = "Reportes", count = "12", icon = Icons.Outlined.Pets)
                StatCard(modifier = Modifier.weight(1f), label = "Ayudados", count = "5", icon = Icons.Outlined.VolunteerActivism)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- OPCIONES DE MENÚ ---
            Text("Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(title = "Mis Mascotas", icon = Icons.Outlined.Pets)
            ProfileMenuItem(title = "Notificaciones", icon = Icons.Outlined.Notifications)
            ProfileMenuItem(
                title = "Cerrar Sesión",
                icon = Icons.AutoMirrored.Outlined.Logout,
                color = Color(0xFFEF5350)
            ) {
                auth.signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Feed.route) { inclusive = true }
                }
            }
        }

        // --- NAVBAR BURBUJA ---
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
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate(Screen.Feed.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Inicio",
                            modifier = Modifier.size(26.dp),
                            tint = Color(0xFF8E8E93)
                        )
                    }

                    IconButton(onClick = { navController.navigate(Screen.AISearch.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Reportar",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF8E8E93)
                        )
                    }

                    IconButton(onClick = { /* Ya estamos acá */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(26.dp),
                            tint = orangePaw
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, count: String, icon: ImageVector) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, color: Color = Color.Black, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
