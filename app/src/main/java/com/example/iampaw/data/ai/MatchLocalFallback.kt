package com.example.iampaw.data.ai

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft

/**
 * Respaldo local cuando Gemini no responde — permite completar el flujo en demo/TPO.
 * No reemplaza a Gemini; usa solo datos del formulario y candidatos en Room.
 */
internal object MatchLocalFallback {

    enum class Reason {
        OFFLINE,
        GEMINI_UNAVAILABLE
    }

    fun analyze(
        draft: ReportDraft,
        candidates: List<DogPost>,
        reason: Reason = Reason.GEMINI_UNAVAILABLE
    ): GeminiMatchResult {
        val analysis = buildString {
            append("Reporte: ${draft.breed}")
            if (draft.colorText.isNotBlank()) append(", color ${draft.colorText}")
            if (draft.sizeText.isNotBlank()) append(", tamaño ${draft.sizeText}")
            append(". Ubicación: ${draft.location}. ")
            append(
                when (reason) {
                    Reason.OFFLINE ->
                        "(Análisis técnico local — sin WiFi/datos; comparación por raza, zona y datos del formulario.)"
                    Reason.GEMINI_UNAVAILABLE ->
                        "(Análisis local — Gemini no estuvo disponible; revisá coincidencias sugeridas abajo.)"
                }
            )
        }
        val matches = candidates
            .map { candidate ->
                ScoredMatch(
                    postId = candidate.id,
                    matchPercentage = scoreCandidate(draft, candidate),
                    reason = buildReason(draft, candidate)
                )
            }
            .sortedByDescending { it.matchPercentage }

        return GeminiMatchResult(aiAnalysis = analysis, matches = matches)
    }

    private fun scoreCandidate(draft: ReportDraft, candidate: DogPost): Int {
        var score = 55
        if (draft.breed.equals(candidate.breed, ignoreCase = true)) score += 25
        else if (draft.breed.lowercase() in candidate.breed.lowercase() ||
            candidate.breed.lowercase() in draft.breed.lowercase()
        ) {
            score += 15
        }
        if (draft.colorText.isNotBlank() && candidate.color.isNotBlank() &&
            draft.colorText.lowercase() in candidate.color.lowercase()
        ) {
            score += 10
        }
        if (locationOverlap(draft.location, candidate.location)) score += 10
        if (draft.name.equals(candidate.name, ignoreCase = true)) score += 15
        return score.coerceIn(50, 92)
    }

    private fun buildReason(draft: ReportDraft, candidate: DogPost): String {
        val parts = mutableListOf<String>()
        if (draft.breed.equals(candidate.breed, ignoreCase = true)) {
            parts += "Misma raza"
        }
        if (locationOverlap(draft.location, candidate.location)) {
            parts += "Zona similar"
        }
        if (draft.name.equals(candidate.name, ignoreCase = true)) {
            parts += "Mismo nombre"
        }
        if (parts.isEmpty()) parts += "Estado opuesto en la base"
        return parts.joinToString(" · ")
    }

    private fun locationOverlap(a: String, b: String): Boolean {
        val wordsA = a.lowercase().split(Regex("[\\s,]+")).filter { it.length > 3 }
        val wordsB = b.lowercase().split(Regex("[\\s,]+")).filter { it.length > 3 }
        return wordsA.any { it in wordsB }
    }
}
