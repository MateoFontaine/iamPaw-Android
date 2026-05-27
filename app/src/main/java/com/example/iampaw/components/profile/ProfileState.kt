package com.example.iampaw.components.profile

import android.net.Uri

data class ProfileState(
    val displayName: String = "Usuario de iamPaw",
    val email: String = "Sin correo vinculado",
    val photoUrl: Uri? = null,
    val reportsCount: String = "12",
    val helpedCount: String = "5",
    val isLoggedOut: Boolean = false // Para saber cuándo navegar al Login
)