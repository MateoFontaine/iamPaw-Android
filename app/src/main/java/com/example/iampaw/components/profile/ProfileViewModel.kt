package com.example.iampaw.components.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.ContactPhoneFormatter
import com.example.iampaw.domain.IPawRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: IPawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onContactPhoneChange(value: String) {
        _uiState.update { it.copy(contactPhone = value, phoneSaveMessage = null) }
    }

    fun saveContactPhone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPhone = true, phoneSaveMessage = null) }
            repository.saveUserContactPhone(_uiState.value.contactPhone)
                .onSuccess {
                    val saved = repository.getUserContactPhone()
                    _uiState.update {
                        it.copy(
                            isSavingPhone = false,
                            contactPhone = saved,
                            phoneSaveMessage = "Teléfono guardado. Se usará en tus publicaciones."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSavingPhone = false,
                            phoneSaveMessage = error.message ?: "No se pudo guardar el teléfono"
                        )
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val phone = repository.getUserContactPhone()
                _uiState.value = _uiState.value.copy(
                    displayName = user.displayName ?: "Usuario de iamPaw",
                    email = user.email ?: "Sin correo vinculado",
                    photoUrl = user.photoUrl,
                    contactPhone = phone.ifBlank { "" },
                    contactPhoneDisplay = if (phone.isNotBlank()) {
                        ContactPhoneFormatter.formatForDisplay(phone)
                    } else {
                        ""
                    }
                )
            }
        }
    }
}
