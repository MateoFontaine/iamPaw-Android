package com.example.iampaw.components.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.DogBreed
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

    private val _breeds = MutableStateFlow<List<DogBreed>>(emptyList())
    val breeds: StateFlow<List<DogBreed>> = _breeds.asStateFlow()

    private val _uiState = MutableStateFlow(ReportState())
    val uiState: StateFlow<ReportState> = _uiState.asStateFlow()

    init {
        fetchBreeds()
    }

    private fun fetchBreeds() {
        viewModelScope.launch {
            _breeds.value = repository.getBreeds()
        }
    }

    fun updateBreedText(text: String) { _uiState.value = _uiState.value.copy(breedText = text) }
    fun updateColorText(text: String) { _uiState.value = _uiState.value.copy(colorText = text) }
    fun updateSizeText(text: String) { _uiState.value = _uiState.value.copy(sizeText = text) }
    fun updateDetailsText(text: String) { _uiState.value = _uiState.value.copy(detailsText = text) }
    fun updateLocationText(text: String) { _uiState.value = _uiState.value.copy(locationText = text) }
    fun setLocationLoading(isLoading: Boolean) { _uiState.value = _uiState.value.copy(isLocationLoading = isLoading) }
    fun setLostStatus(isLost: Boolean) { _uiState.value = _uiState.value.copy(isLost = isLost) }
    fun setTempCameraUri(uri: Uri) { _uiState.value = _uiState.value.copy(tempCameraUri = uri) }

    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)

        if (uri != null) {
            viewModelScope.launch {
                val loadingText = "iamPaw AI analizando..."
                _uiState.value = _uiState.value.copy(
                    breedText = loadingText,
                    colorText = loadingText,
                    sizeText = loadingText
                )

                delay(4000)

                _uiState.value = _uiState.value.copy(
                    breedText = "Golden Retriever",
                    colorText = "Dorado / Crema",
                    sizeText = "Grande (aprox 30kg)"
                )
            }
        }
    }
}
