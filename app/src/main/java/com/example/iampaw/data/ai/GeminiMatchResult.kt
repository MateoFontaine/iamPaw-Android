package com.example.iampaw.data.ai

data class GeminiMatchResult(
    val aiAnalysis: String,
    val matches: List<ScoredMatch>
)

data class ScoredMatch(
    val postId: String,
    val matchPercentage: Int,
    val reason: String
)
