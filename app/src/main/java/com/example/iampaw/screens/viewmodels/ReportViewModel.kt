package com.example.iampaw.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.DogBreed
import com.example.iampaw.data.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    private val _breeds = MutableStateFlow<List<DogBreed>>(emptyList())
    val breeds: StateFlow<List<DogBreed>> = _breeds

    var selectedBreed = MutableStateFlow("")
    var selectedImageUri = MutableStateFlow<android.net.Uri?>(null)
    var description = MutableStateFlow("")

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchBreeds()
    }

    private fun fetchBreeds() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val response = RetrofitInstance.api.getBreeds()
                _breeds.value = response
                Log.d("API_TEST", "¡Razas cargadas! Se encontraron ${response.size} razas.")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido al conectar con la API"
                Log.e("API_ERROR", "Falló la llamada a The Dog API", e)
            }
        }
    }
}