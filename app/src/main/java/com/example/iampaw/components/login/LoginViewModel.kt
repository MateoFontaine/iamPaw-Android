package com.example.iampaw.components.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    fun signInWithFirebase(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LOGIN_EXITOSO", "Usuario: ${auth.currentUser?.email}")
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                Log.e("LOGIN_ERROR", "Error de Firebase", task.exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = task.exception?.message ?: "Error desconocido"
                )
            }
        }
    }
}
