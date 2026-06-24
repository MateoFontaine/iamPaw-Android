package com.example.iampaw.components.match

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.components.report.ReportDraftStore
import com.example.iampaw.data.ai.GeminiMatchAnalyzer
import com.example.iampaw.data.ai.MatchLocalFallback
import com.example.iampaw.data.local.ReportImageStorage
import com.example.iampaw.data.network.NetworkConnectivity
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val TAG = "MatchViewModel"

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: IPawRepository,
    private val draftStore: ReportDraftStore,
    private val geminiMatchAnalyzer: GeminiMatchAnalyzer,
    private val reportImageStorage: ReportImageStorage,
    private val networkConnectivity: NetworkConnectivity
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchState())
    val uiState: StateFlow<MatchState> = _uiState.asStateFlow()

    init {
        startGeminiAnalysis()
    }

    fun retryAnalysis() {
        _uiState.update { it.copy(isScanning = true, errorMessage = null, publishError = null) }
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
            val image = reportImageStorage.readImageForGemini(draft.imageUrl)
            Log.d(
                TAG,
                "Inicio análisis: status=${draft.status}, candidatos=${candidates.size}, " +
                    "foto=${image != null}, red=${networkConnectivity.hasInternetForAi()}, " +
                    "nombres=${candidates.map { it.name }}"
            )

            if (!networkConnectivity.hasInternetForAi()) {
                if (candidates.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            aiAnalysis = "",
                            matches = emptyList(),
                            locationHint = draft.location,
                            candidateCount = 0,
                            imageSentToAi = image != null,
                            analysisSource = MatchAnalysisSource.LOCAL_OFFLINE,
                            usedLocalFallback = false,
                            infoMessage = buildInfoMessage(
                                candidateCount = 0,
                                imageSent = image != null,
                                analysisSource = MatchAnalysisSource.LOCAL_OFFLINE
                            ),
                            errorMessage = "Sin WiFi ni datos — conectate para usar Gemini.",
                            candidatesEmptyMessage = "No hay reportes con estado opuesto para comparar."
                        )
                    }
                } else {
                    val fallback = MatchLocalFallback.analyze(
                        draft,
                        candidates,
                        MatchLocalFallback.Reason.OFFLINE
                    )
                    applyAnalysisResult(
                        draft = draft,
                        candidates = candidates,
                        imageSent = image != null,
                        result = fallback,
                        analysisSource = MatchAnalysisSource.LOCAL_OFFLINE
                    )
                }
                return@launch
            }

            geminiMatchAnalyzer.analyzeReport(draft, candidates, image)
                .onSuccess { result ->
                    applyAnalysisResult(
                        draft = draft,
                        candidates = candidates,
                        imageSent = image != null,
                        result = result,
                        analysisSource = MatchAnalysisSource.GEMINI
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Fallo Gemini: ${error.message}", error)
                    if (candidates.isNotEmpty()) {
                        val fallback = MatchLocalFallback.analyze(
                            draft,
                            candidates,
                            MatchLocalFallback.Reason.GEMINI_UNAVAILABLE
                        )
                        applyAnalysisResult(
                            draft = draft,
                            candidates = candidates,
                            imageSent = image != null,
                            result = fallback,
                            analysisSource = MatchAnalysisSource.LOCAL_GEMINI_FAILED,
                            fallbackNote = error.message
                        )
                    } else {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                aiAnalysis = "",
                                matches = emptyList(),
                                locationHint = draft.location,
                                candidateCount = 0,
                                imageSentToAi = image != null,
                                analysisSource = MatchAnalysisSource.LOCAL_GEMINI_FAILED,
                                usedLocalFallback = false,
                                infoMessage = null,
                                errorMessage = error.message
                                    ?: "No se pudo analizar con iamPaw AI. Revisá tu conexión.",
                                candidatesEmptyMessage = "No hay reportes con estado opuesto para comparar."
                            )
                        }
                    }
                }
        }
    }

    private fun applyAnalysisResult(
        draft: ReportDraft,
        candidates: List<DogPost>,
        imageSent: Boolean,
        result: com.example.iampaw.data.ai.GeminiMatchResult,
        analysisSource: MatchAnalysisSource,
        fallbackNote: String? = null
    ) {
        val usedFallback = analysisSource != MatchAnalysisSource.GEMINI
        val matches = MatchCandidateMapper.mapScoredMatches(result.matches, candidates)
        val infoMessage = buildInfoMessage(
            candidateCount = candidates.size,
            imageSent = imageSent,
            aiAnalysis = result.aiAnalysis,
            rawMatchCount = result.matches.size,
            mappedMatchCount = matches.size,
            analysisSource = analysisSource,
            fallbackNote = fallbackNote
        )
        Log.d(
            TAG,
            "Resultado: source=$analysisSource, análisis=${result.aiAnalysis.length} chars, matches=${matches.size}"
        )

        _uiState.update {
            it.copy(
                isScanning = false,
                aiAnalysis = result.aiAnalysis,
                matches = matches,
                locationHint = draft.location,
                candidateCount = candidates.size,
                imageSentToAi = imageSent,
                analysisSource = analysisSource,
                usedLocalFallback = usedFallback,
                infoMessage = infoMessage,
                candidatesEmptyMessage = if (candidates.isEmpty()) {
                    "No hay reportes con estado opuesto para comparar. Publicá tu alerta para que otros la vean."
                } else {
                    null
                },
                errorMessage = null
            )
        }
    }

    private fun buildInfoMessage(
        candidateCount: Int,
        imageSent: Boolean,
        aiAnalysis: String = "",
        rawMatchCount: Int = 0,
        mappedMatchCount: Int = 0,
        analysisSource: MatchAnalysisSource,
        fallbackNote: String? = null
    ): String {
        val parts = mutableListOf<String>()
        parts += "Candidatos opuestos en la base: $candidateCount"
        parts += if (imageSent) "Foto lista: sí" else "Foto lista: no"
        when (analysisSource) {
            MatchAnalysisSource.GEMINI -> {
                parts += "Análisis con Gemini: OK"
                if (mappedMatchCount > 0) parts += "Coincidencias: $mappedMatchCount"
                else if (candidateCount > 0 && rawMatchCount == 0) {
                    parts += "Gemini no sugirió coincidencias"
                }
            }
            MatchAnalysisSource.LOCAL_OFFLINE -> {
                parts += "Sin WiFi/datos — análisis técnico local"
                if (mappedMatchCount > 0) parts += "Coincidencias: $mappedMatchCount"
            }
            MatchAnalysisSource.LOCAL_GEMINI_FAILED -> {
                parts += "Modo respaldo local (Gemini no respondió)"
                fallbackNote?.let { parts += it }
                if (mappedMatchCount > 0) parts += "Coincidencias: $mappedMatchCount"
            }
        }
        return parts.joinToString(" · ")
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
}
