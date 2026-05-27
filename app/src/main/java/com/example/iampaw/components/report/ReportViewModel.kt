package com.example.iampaw.components.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.DogBreed
import com.example.iampaw.data.PawRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    // 1. Instanciamos el repositorio (usará los defaults: Mock y API)
    private val repository = PawRepository()

    // --- ESTADO DE LA API DE RAZAS ---
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
            // 2. Consumimos los datos a través del repositorio
            val response = repository.getBreeds()
            _breeds.value = response
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