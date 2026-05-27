package com.example.iampaw.components.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser

        if (user != null) {
            _uiState.value = _uiState.value.copy(
                displayName = user.displayName ?: "Usuario de iamPaw",
                email = user.email ?: "Sin correo vinculado",
                photoUrl = user.photoUrl
            )
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }
}