package com.example.iampaw.components

sealed class Screen(val route: String) {
    // La pantalla de carga inicial
    object Splash : Screen("splash_screen")

    // Autenticación
    object Login : Screen("login_screen")

    // El CU-03: La lista general de mascotas perdidas/encontradas
    object Feed : Screen("feed_screen")

    // La pantalla donde el usuario sube la foto para que la IA busque coincidencias
    object AISearch : Screen("ai_search_screen")

    // El detalle del match o de la mascota específica
    object Detail : Screen("detail_screen")

    // Perfil del usuario
    object Profile : Screen("profile_screen")
}