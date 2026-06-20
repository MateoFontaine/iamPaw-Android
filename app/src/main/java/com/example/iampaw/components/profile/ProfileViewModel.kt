package com.example.iampaw.components.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

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
