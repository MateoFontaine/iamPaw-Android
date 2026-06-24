package com.example.iampaw.components.match

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.iampaw.components.Screen
import com.example.iampaw.components.commons.ReportGlideImage
import com.example.iampaw.components.commons.reportImageModel

@Composable
fun MatchScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val orangePaw = Color(0xFFFF9800)
    val bgColor = Color(0xFFFBFBFB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Outlined.Close, contentDescription = "Cancelar", tint = Color.Black)
            }
            Text(
                text = if (state.isScanning) "Analizando reporte..." else "Coincidencias Detectadas",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isScanning) {
                ScanningAnimation(orangePaw)
            } else {
                MatchResultsList(
                    color = orangePaw,
                    navController = navController,
                    viewModel = viewModel,
                    state = state
                )
            }
        }
    }
}

@Composable
fun ScanningAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = "IA",
                modifier = Modifier.size(56.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "iamPaw AI está escaneando los píxeles...",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        Text(
            text = "Cruzando rasgos visuales y geolocalización",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MatchResultsList(
    color: Color,
    navController: NavController,
    viewModel: MatchViewModel,
    state: MatchState
) {
    val locationLabel = state.locationHint.ifBlank { "tu zona" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.errorMessage?.let { error ->
            item(key = "match_error") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.retryAnalysis() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isScanning
                        ) {
                            Text("Reintentar con Gemini")
                        }
                    }
                }
            }
        }

        if (state.analysisSource == MatchAnalysisSource.LOCAL_OFFLINE && state.errorMessage == null) {
            item(key = "offline_notice") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sin WiFi ni datos — coincidencias por análisis técnico local (raza, zona, formulario).",
                            color = Color(0xFF455A64),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.retryAnalysis() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isScanning
                        ) {
                            Text("Reintentar con Gemini")
                        }
                    }
                }
            }
        }

        if (state.analysisSource == MatchAnalysisSource.LOCAL_GEMINI_FAILED && state.errorMessage == null) {
            item(key = "fallback_notice") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gemini no respondió — mostramos coincidencias en modo respaldo local.",
                            color = Color(0xFFE65100),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.retryAnalysis() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isScanning
                        ) {
                            Text("Reintentar con Gemini")
                        }
                    }
                }
            }
        }

        item(key = "match_intro") {
            val intro = when {
                state.matches.isNotEmpty() && state.analysisSource == MatchAnalysisSource.LOCAL_OFFLINE ->
                    "Coincidencias sugeridas por análisis técnico local (sin red). Conectate y tocá «Reintentar con Gemini»:"
                state.matches.isNotEmpty() && state.analysisSource == MatchAnalysisSource.LOCAL_GEMINI_FAILED ->
                    "Coincidencias sugeridas en modo respaldo (Gemini no respondió). Revisá o reintentá:"
                state.matches.isNotEmpty() ->
                    "Encontramos reportes activos cerca de $locationLabel que podrían coincidir:"
                state.candidatesEmptyMessage != null -> state.candidatesEmptyMessage
                else -> "No se encontraron coincidencias claras con reportes existentes en $locationLabel."
            }
            Text(
                text = intro,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(state.matches, key = { it.postId }) { matchedDog ->
            MatchCard(
                matchedDog = matchedDog,
                color = color,
                navController = navController
            )
        }

        item(key = "publish_section") {
            Spacer(modifier = Modifier.height(16.dp))

            state.publishError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.publishReport {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = !state.isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Ninguno es mi mascota. Publicar Alerta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MatchCard(
    matchedDog: MatchedDog,
    color: Color,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (matchedDog.reason.isNotBlank()) 240.dp else 220.dp)
            .clickable {
                navController.navigate(Screen.Detail.createRoute(matchedDog.postId))
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ReportGlideImage(
                model = reportImageModel(matchedDog.imageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            val badgeBgColor = if (matchedDog.matchPercentage > 90) Color(0xFF4CAF50) else color
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${matchedDog.matchPercentage}% Match",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = matchedDog.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Text(
                    text = matchedDog.breed,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (matchedDog.reason.isNotBlank()) {
                    Text(
                        text = matchedDog.reason,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${matchedDog.location} • ${matchedDog.timeText}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
