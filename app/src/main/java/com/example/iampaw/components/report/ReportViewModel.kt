package com.example.iampaw.components.report

import android.net.Uri
import android.util.Log
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

private const val TAG = "ReportViewModel"

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: IPawRepository,
    private val draftStore: ReportDraftStore
) : ViewModel() {

    private val defaultBreeds = listOf(
        DogBreed(id = -1, name = "Mestizo"),
        DogBreed(id = -2, name = "Calle"),
        DogBreed(id = -3, name = "Golden Retriever"),
        DogBreed(id = -4, name = "Labrador Retriever"),
        DogBreed(id = -5, name = "Caniche"),
        DogBreed(id = -6, name = "Bulldog Francés"),
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
            if (apiBreeds.isEmpty()) {
                Log.w(TAG, "The Dog API no devolvió razas (revisá DOG_API_KEY en local.properties)")
            } else {
                Log.d(TAG, "Razas cargadas desde API: ${apiBreeds.size}")
            }
            _breeds.update { current ->
                (current + apiBreeds).distinctBy { it.name.lowercase() }
            }
        }
    }

    fun setLocationLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLocationLoading = isLoading) }
    }

    fun setLostStatus(isLost: Boolean) {
        _uiState.update { it.copy(isLost = isLost, validationError = null) }
    }

    fun setTempCameraUri(uri: Uri) {
        _uiState.update { it.copy(tempCameraUri = uri) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun proceedToMatch(form: ReportFormInput, onReady: () -> Unit) {
        val state = _uiState.value
        val validationError = when {
            state.isLost && form.nameText.isBlank() -> "Ingresá el nombre de tu mascota"
            form.breedText.isBlank() -> "Ingresá la raza"
            form.locationText.isBlank() -> "Ingresá la ubicación"
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        val displayName = form.nameText.trim().ifBlank { form.breedText.trim() }

        draftStore.save(
            ReportDraft(
                name = displayName,
                breed = form.breedText.trim(),
                location = form.locationText.trim(),
                imageUrl = state.imageUri?.toString().orEmpty(),
                status = if (state.isLost) "Perdido" else "Encontrado",
                colorText = form.colorText.trim(),
                sizeText = form.sizeText.trim(),
                detailsText = form.detailsText.trim()
            )
        )

        _uiState.update { it.copy(validationError = null) }
        onReady()
    }
}
