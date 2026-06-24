package com.example.iampaw.components.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraftStore
import com.example.iampaw.data.ai.GeminiMatchAnalyzer
import com.example.iampaw.data.ai.ScoredMatch
import com.example.iampaw.data.local.ReportImageStorage
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val draftStore: ReportDraftStore,
    private val geminiMatchAnalyzer: GeminiMatchAnalyzer,
    private val reportImageStorage: ReportImageStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchState())
    val uiState: StateFlow<MatchState> = _uiState.asStateFlow()

    init {
        startGeminiAnalysis()
    }

    private fun startGeminiAnalysis() {
        viewModelScope.launch {
            val draft = draftStore.peek()
            if (draft == null) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "No hay datos del reporte. Volvé atrás y completá el formulario."
                    )
                }
                return@launch
            }

            val candidates = repository.getMatchCandidates(draft)
            val imageBytes = reportImageStorage.readImageBytes(draft.imageUrl)

            geminiMatchAnalyzer.analyzeReport(draft, candidates, imageBytes)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            aiAnalysis = result.aiAnalysis,
                            matches = mapScoredMatches(result.matches, candidates),
                            locationHint = draft.location,
                            candidatesEmptyMessage = if (candidates.isEmpty()) {
                                "No hay reportes con estado opuesto para comparar. " +
                                    "Publicá tu alerta para que otros la vean."
                            } else {
                                null
                            },
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            aiAnalysis = "",
                            matches = emptyList(),
                            locationHint = draft.location,
                            errorMessage = error.message
                                ?: "No se pudo analizar con iamPaw AI. Revisá tu conexión."
                        )
                    }
                }
        }
    }

    fun publishReport(onSuccess: () -> Unit) {
        val draft = draftStore.peek()
        if (draft == null) {
            _uiState.update {
                it.copy(publishError = "No hay datos del reporte. Volvé atrás y completá el formulario.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, publishError = null) }

            val aiAnalysis = _uiState.value.aiAnalysis
            val report = DogPost(
                id = UUID.randomUUID().toString(),
                name = draft.name,
                breed = draft.breed,
                location = draft.location,
                time = "Recién",
                imageUrl = draft.imageUrl,
                status = draft.status,
                color = draft.colorText,
                size = draft.sizeText,
                details = draft.detailsText,
                aiAnalysis = aiAnalysis
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

    companion object {
        internal fun mapScoredMatches(
            scored: List<ScoredMatch>,
            candidates: List<DogPost>
        ): List<MatchedDog> {
            val candidateById = candidates.associateBy { it.id }
            return scored.mapNotNull { match ->
                val post = candidateById[match.postId] ?: return@mapNotNull null
                MatchedDog(
                    postId = post.id,
                    name = post.name,
                    breed = post.breed,
                    location = post.location,
                    timeText = post.time,
                    matchPercentage = match.matchPercentage,
                    imageUrl = post.imageUrl,
                    reason = match.reason
                )
            }.sortedByDescending { it.matchPercentage }
        }
    }
}
