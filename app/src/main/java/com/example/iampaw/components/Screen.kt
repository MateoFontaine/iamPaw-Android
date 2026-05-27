package com.example.iampaw.components

sealed class Screen(val route: String) {

    object Splash : Screen("splash_screen")


    object Login : Screen("login_screen")


    object Feed : Screen("feed_screen")


    object AISearch : Screen("ai_search_screen")


    object Detail : Screen("detail_screen")


    object Profile : Screen("profile_screen")
}