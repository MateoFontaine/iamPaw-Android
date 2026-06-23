package com.example.iampaw.components.match

data class MatchedDog(
    val name: String,
    val breed: String,
    val location: String,
    val timeText: String,
    val matchPercentage: Int,
    val imageUrl: String
)

data class MatchState(
    val isScanning: Boolean = true,
    val matches: List<MatchedDog> = emptyList(),
    val locationHint: String = "",
    val isPublishing: Boolean = false,
    val publishError: String? = null
)
