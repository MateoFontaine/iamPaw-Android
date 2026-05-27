package com.example.iampaw.components.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    // Sirve para prender la ruedita apenas tocamos el botón (antes de que se abra el pop-up de Google)
    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    // Recibe el token de Google y hace el login real en Firebase
    fun signInWithFirebase(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LOGIN_EXITOSO", "Usuario: ${auth.currentUser?.email}")
                // Avisamos que fue un éxito para que la pantalla navegue al Feed
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                Log.e("LOGIN_ERROR", "Error de Firebase", task.exception)
                // Apagamos la ruedita y guardamos el error
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = task.exception?.message ?: "Error desconocido"
                )
            }
        }
    }
}