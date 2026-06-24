package com.example.iampaw.data.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

internal object GeminiResponseParser {

    fun parse(rawText: String, gson: Gson = Gson()): Result<GeminiMatchResult> {
        val json = extractJson(rawText)
        return try {
            val dto = gson.fromJson(json, GeminiMatchResponseDto::class.java)
                ?: return Result.failure(IllegalStateException("Respuesta JSON vacía"))
            val matches = dto.matches.orEmpty().mapNotNull { match ->
                val postId = match.postId?.trim().orEmpty()
                if (postId.isEmpty()) return@mapNotNull null
                ScoredMatch(
                    postId = postId,
                    matchPercentage = match.matchPercentage?.coerceIn(0, 100) ?: 0,
                    reason = match.reason?.trim().orEmpty()
                )
            }
            Result.success(
                GeminiMatchResult(
                    aiAnalysis = dto.aiAnalysis?.trim().orEmpty(),
                    matches = matches
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo parsear la respuesta de Gemini", e))
        }
    }

    internal fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        val fencePattern = Regex("""```(?:json)?\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE)
        fencePattern.find(trimmed)?.groupValues?.get(1)?.trim()?.let { return it }

        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1)
        }
        return trimmed
    }
}

private data class GeminiMatchResponseDto(
    @SerializedName("aiAnalysis") val aiAnalysis: String?,
    @SerializedName("matches") val matches: List<ScoredMatchDto>?
)

private data class ScoredMatchDto(
    @SerializedName("postId") val postId: String?,
    @SerializedName("matchPercentage") val matchPercentage: Int?,
    @SerializedName("reason") val reason: String?
)
