package com.example.iampaw.components.report

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.DogBreed // Asegurate de que este import coincida con el tuyo
import com.example.iampaw.data.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    // --- ESTADO DE LA API DE RAZAS (Lo que ya tenías) ---
    private val _breeds = MutableStateFlow<List<DogBreed>>(emptyList())
    val breeds: StateFlow<List<DogBreed>> = _breeds.asStateFlow()

    // --- ESTADO DEL FORMULARIO ---
    private val _uiState = MutableStateFlow(ReportState())
    val uiState: StateFlow<ReportState> = _uiState.asStateFlow()

    init {
        fetchBreeds()
    }

    private fun fetchBreeds() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getBreeds()
                _breeds.value = response
            } catch (e: Exception) {
                Log.e("API_ERROR", "Falló la llamada a The Dog API", e)
            }
        }
    }

    // --- FUNCIONES PARA ACTUALIZAR EL FORMULARIO ---
    fun updateBreedText(text: String) { _uiState.value = _uiState.value.copy(breedText = text) }
    fun updateColorText(text: String) { _uiState.value = _uiState.value.copy(colorText = text) }
    fun updateSizeText(text: String) { _uiState.value = _uiState.value.copy(sizeText = text) }
    fun updateDetailsText(text: String) { _uiState.value = _uiState.value.copy(detailsText = text) }
    fun updateLocationText(text: String) { _uiState.value = _uiState.value.copy(locationText = text) }
    fun setLocationLoading(isLoading: Boolean) { _uiState.value = _uiState.value.copy(isLocationLoading = isLoading) }
    fun setLostStatus(isLost: Boolean) { _uiState.value = _uiState.value.copy(isLost = isLost) }
    fun setTempCameraUri(uri: Uri) { _uiState.value = _uiState.value.copy(tempCameraUri = uri) }

    // --- LA MAGIA DE LA IA (Mago de Oz) ---
    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)

        if (uri != null) {
            viewModelScope.launch {
                // 1. Feedback visual
                val loadingText = "iamPaw AI analizando..."
                _uiState.value = _uiState.value.copy(
                    breedText = loadingText,
                    colorText = loadingText,
                    sizeText = loadingText
                )

                // 2. Latencia simulada
                delay(4000)

                // 3. Resultado de la "IA"
                _uiState.value = _uiState.value.copy(
                    breedText = "Golden Retriever",
                    colorText = "Dorado / Crema",
                    sizeText = "Grande (aprox 30kg)"
                )
            }
        }
    }
}