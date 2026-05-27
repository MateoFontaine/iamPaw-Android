package com.example.iampaw.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.iampaw.R
import com.example.iampaw.components.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val interFamily = FontFamily(
        Font(R.font.inter_extrabold, FontWeight.ExtraBold)
    )

    // --- NUEVO ESTADO PARA LA RUEDITA DE CARGA ---
    var isLoading by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("892894302329-uuio0fu0c0keehrj4es5b0dr8nm4jdnd.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            // Acá arranca la conexión con Firebase, prendemos la ruedita
            isLoading = true

            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d("LOGIN_EXITOSO", "Usuario: ${auth.currentUser?.email}")
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    // Ojo: no hace falta apagar la ruedita acá porque ya cambiamos de pantalla
                } else {
                    // Si falló Firebase, apagamos la ruedita para que pueda volver a intentar
                    isLoading = false
                    Log.e("LOGIN_ERROR", "Error de Firebase", authTask.exception)
                }
            }
        } catch (e: ApiException) {
            // Si el usuario cerró el pop-up de Google sin elegir cuenta, apagamos la ruedita
            isLoading = false
            Log.e("LOGIN_ERROR", "Error de Google o cancelación", e)
        }
    }

    val googleGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4285F4), // Azul
            Color(0xFFEA4335), // Rojo
            Color(0xFFFBBC05), // Amarillo
            Color(0xFF34A853)  // Verde
        )
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF9F0),
            Color(0xFFFBFDFF),
            Color(0xFFF0F6FF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "iam", fontSize = 42.sp, fontFamily = interFamily, color = Color.Black)
                Text(text = "Paw", fontSize = 42.sp, fontFamily = interFamily, color = Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Textos
            Text(
                text = "¡Te damos la bienvenida!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unite a la comunidad de iamPaw y empezá a reportar para hacer del mundo un lugar mejor.",
                fontSize = 16.sp,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // --- BOTÓN ACTUALIZADO CON ESTADO DE CARGA ---
            Button(
                onClick = {
                    if (!isLoading) { // Evitamos que haga doble click mientras carga
                        isLoading = true // Prendemos la ruedita antes de que salga el pop-up
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 1.5.dp,
                        brush = googleGradientBrush,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                // Si está cargando mostramos la ruedita, sino el logo y texto de siempre
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Logo de Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continuar con Google",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}