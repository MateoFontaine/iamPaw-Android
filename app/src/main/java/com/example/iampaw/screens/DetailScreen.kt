package com.example.iampaw.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun DetailScreen(navController: NavController) {
    val context = LocalContext.current
    val orangePaw = Color(0xFFFF9800)
    val bgColor = Color(0xFFFBFBFB)


    val name = "Bobby"
    val breed = "Golden Retriever"
    val location = "Pinamar Centro"
    val imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600"
    val description = "Se perdió cerca de la plaza central. Es súper manso pero está asustado. Tiene un collar de cuero marrón sin chapita."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )


            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 8.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = Color.Black)
            }
        }

        // --- CUERPO DE LA INFORMACIÓN ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Badge de Estado
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "PERDIDO",
                    color = Color(0xFFD32F2F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Nombre y Raza
            Text(text = name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(text = breed, fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))

            // Ubicación Corta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = orangePaw, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = location, color = Color.DarkGray, fontSize = 14.sp)
            }

            Divider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFEEEEEE))

            // --- BLOQUE ANÁLISIS DE IA (EXIGIDO EN EL CU-05) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = "IA", tint = orangePaw)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Análisis de Rasgos por iamPaw AI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Tamaño: Grande\n• Color: Dorado / Crema\n• Particularidades: Collar de cuero", fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Descripción del usuario
            Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- ACCIONES IMPLÍCITAS (INTENTS EXIGIDOS EN EL PDF) ---
            // Botón Google Maps
            OutlinedButton(
                onClick = {
                    // Intent Implícito para abrir Google Maps mapeando la zona
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location, Pinamar, Argentina"))
                    context.startActivity(mapIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Zona en Mapa", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Contactar por WhatsApp
            Button(
                onClick = {
                    val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=5492254000000&text=Hola! Vengo de iamPaw, creo que vi a tu mascota Bobby."))
                    context.startActivity(whatsappIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = orangePaw)
            ) {
                Icon(Icons.Outlined.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contactar al Dueño (WhatsApp)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}