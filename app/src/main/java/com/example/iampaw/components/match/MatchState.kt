package com.example.iampaw.components.match

data class MatchedDog(
    val postId: String,
    val name: String,
    val breed: String,
    val location: String,
    val timeText: String,
    val matchPercentage: Int,
    val imageUrl: String,
    val reason: String = ""
)

data class MatchState(
    val isScanning: Boolean = true,
    val matches: List<MatchedDog> = emptyList(),
    val locationHint: String = "",
    val aiAnalysis: String = "",
    val candidateCount: Int = 0,
    val imageSentToAi: Boolean = false,
    val analysisSource: MatchAnalysisSource = MatchAnalysisSource.GEMINI,
    val usedLocalFallback: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val candidatesEmptyMessage: String? = null,
    val isPublishing: Boolean = false,
    val publishError: String? = null
)
