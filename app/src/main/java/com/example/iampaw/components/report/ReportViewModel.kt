package com.example.iampaw.components.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.DogBreed
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: IPawRepository,
    private val draftStore: ReportDraftStore
) : ViewModel() {

    private val defaultBreeds = listOf(
        DogBreed(id = -1, name = "Mestizo"),
        DogBreed(id = -2, name = "Calle"),
    )

    private val _breeds = MutableStateFlow(defaultBreeds)
    val breeds: StateFlow<List<DogBreed>> = _breeds.asStateFlow()

    private val _uiState = MutableStateFlow(ReportState())
    val uiState: StateFlow<ReportState> = _uiState.asStateFlow()

    init {
        fetchBreeds()
    }

    private fun fetchBreeds() {
        viewModelScope.launch {
            val apiBreeds = repository.getBreeds()
            _breeds.update { current ->
                (current + apiBreeds).distinctBy { it.name.lowercase() }
            }
        }
    }

    fun updateNameText(text: String) { _uiState.update { it.copy(nameText = text) } }
    fun updateBreedText(text: String) { _uiState.update { it.copy(breedText = text) } }
    fun updateColorText(text: String) { _uiState.update { it.copy(colorText = text) } }
    fun updateSizeText(text: String) { _uiState.update { it.copy(sizeText = text) } }
    fun updateDetailsText(text: String) { _uiState.update { it.copy(detailsText = text) } }
    fun updateLocationText(text: String) { _uiState.update { it.copy(locationText = text) } }
    fun setLocationLoading(isLoading: Boolean) { _uiState.update { it.copy(isLocationLoading = isLoading) } }
    fun setLostStatus(isLost: Boolean) { _uiState.update { it.copy(isLost = isLost) } }
    fun setTempCameraUri(uri: Uri) { _uiState.update { it.copy(tempCameraUri = uri) } }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun proceedToMatch(onReady: () -> Unit) {
        val state = _uiState.value
        val validationError = when {
            state.isLost && state.nameText.isBlank() -> "Ingresá el nombre de tu mascota"
            state.breedText.isBlank() -> "Ingresá la raza"
            state.locationText.isBlank() -> "Ingresá la ubicación"
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        val displayName = state.nameText.trim().ifBlank { state.breedText.trim() }

        draftStore.save(
            ReportDraft(
                name = displayName,
                breed = state.breedText.trim(),
                location = state.locationText.trim(),
                imageUrl = state.imageUri?.toString().orEmpty(),
                status = if (state.isLost) "Perdido" else "Encontrado",
                colorText = state.colorText.trim(),
                sizeText = state.sizeText.trim(),
                detailsText = state.detailsText.trim()
            )
        )

        _uiState.update { it.copy(validationError = null) }
        onReady()
    }
}
