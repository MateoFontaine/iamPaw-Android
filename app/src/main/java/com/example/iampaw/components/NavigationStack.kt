package com.example.iampaw.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationStack(navController: NavHostController) {
    // NavHost es el escenario.
    // startDestination dicta que la app arranca siempre en el Splash Screen (CU-01)
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // CU-01: Splash Screen
        composable(route = Screen.Splash.route) {
            // Acá adentro después llamaremos a la vista real (ej: SplashScreen(navController))
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