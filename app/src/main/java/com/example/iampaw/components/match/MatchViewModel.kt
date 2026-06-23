package com.example.iampaw.components.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraftStore
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: IPawRepository,
    private val draftStore: ReportDraftStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchState())
    val uiState: StateFlow<MatchState> = _uiState.asStateFlow()

    init {
        startScanningSimulation()
    }

    private fun startScanningSimulation() {
        viewModelScope.launch {
            delay(3500)

            _uiState.update {
                it.copy(
                    isScanning = false,
                    matches = repository.getMatchedDogs(),
                    locationHint = draftStore.peek()?.location.orEmpty()
                )
            }
        }
    }

    fun publishReport(onSuccess: () -> Unit) {
        val draft = draftStore.peek()
        if (draft == null) {
            _uiState.update { it.copy(publishError = "No hay datos del reporte. Volvé atrás y completá el formulario.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, publishError = null) }

            val report = DogPost(
                id = UUID.randomUUID().toString(),
                name = draft.name,
                breed = draft.breed,
                location = draft.location,
                time = "Recién",
                imageUrl = draft.imageUrl,
                status = draft.status
            )

            repository.saveReport(report)
                .onSuccess {
                    draftStore.consume()
                    _uiState.update { it.copy(isPublishing = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPublishing = false,
                            publishError = error.message ?: "No se pudo publicar el reporte"
                        )
                    }
                }
        }
    }
}
