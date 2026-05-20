package com.example.iampaw.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Requisito para el efecto secundario
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.iampaw.R
import com.example.iampaw.components.Screen // Asegurate de apuntar a la ruta de tu enum/sealed class de rutas
import kotlinx.coroutines.delay // Requisito para pausar el tiempo

@Composable
fun SplashScreen(navController: NavController) {

    val interFamily = FontFamily(
        Font(R.font.inter_extrabold, FontWeight.ExtraBold)
    )

    LaunchedEffect(key1 = true) {
        delay(3000L)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF9800)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "iam",
                fontSize = 48.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Paw",
                fontSize = 48.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }
    }
}