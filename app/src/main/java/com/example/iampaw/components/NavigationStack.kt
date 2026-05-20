package com.example.iampaw.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// 1. Android Studio te va a pedir importar tu pantalla (suele agregarse sola)
import com.example.iampaw.screens.SplashScreen

@Composable
fun NavigationStack(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // CU-01: Splash Screen
        composable(route = Screen.Splash.route) {
            // 2. ¡Acá va la línea mágica! Llamamos a la vista que acabas de crear
            SplashScreen(navController = navController)
        }

        // CU-02: Autenticación con Google
        composable(route = Screen.Login.route) {
            // LoginScreen(navController)
        }

        // CU-03: Feed Principal
        composable(route = Screen.Feed.route) {
            // FeedScreen(navController)
        }

        // CU-04: Reporte Inteligente (Cámara + IA)
        composable(route = Screen.AISearch.route) {
            // AISearchScreen(navController)
        }

        // CU-05: Detalle del Reporte
        composable(route = Screen.Detail.route) {
            // DetailScreen(navController)
        }
    }
}