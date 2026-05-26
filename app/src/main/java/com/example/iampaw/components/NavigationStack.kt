package com.example.iampaw.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.iampaw.screens.FeedScreen
import com.example.iampaw.screens.SplashScreen
import com.example.iampaw.screens.LoginScreen
import com.example.iampaw.screens.ReportScreen
import com.example.iampaw.screens.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.iampaw.screens.MatchScreen
import com.example.iampaw.screens.DetailScreen


@Composable
fun NavigationStack(navController: NavHostController) {
    // 1. Le preguntamos a Firebase si ya hay un usuario guardado
    val currentUser = FirebaseAuth.getInstance().currentUser

    // 2. Definimos cuál es la pantalla inicial según el estado del usuario
    val initialScreen = if (currentUser != null) {
        Screen.Feed.route // Si hay sesión abierta, va directo al feed
    } else {
        Screen.Splash.route // Si es la primera vez o cerró sesión, va al Splash
    }

    NavHost(
        navController = navController,
        startDestination = initialScreen,
        // --- ANIMACIONES REMOVIDAS PARA CAMBIO INSTANTÁNEO ---
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        // CU-01: Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // CU-02: Autenticación con Google
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // CU-03: Feed Principal
        composable(route = Screen.Feed.route) {
            FeedScreen(navController = navController)
        }

        // CU-04: Reporte Inteligente (Cámara + IA)
        composable(route = Screen.AISearch.route) {
            ReportScreen(navController = navController)
        }

        // CU-05: Detalle del Reporte
        composable(route = Screen.Detail.route) {
            DetailScreen(navController = navController)
        }

        // CU-06: Perfil del Usuario
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        // Pantalla de Match de Gemini (Simulación IA)
        composable(route = "match_screen") {
            MatchScreen(navController = navController)
        }
    }
}