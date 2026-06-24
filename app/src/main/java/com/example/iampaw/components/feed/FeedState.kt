package com.example.iampaw.components.feed

data class DogPost(
    val id: String,
    val name: String,
    val breed: String,
    val location: String,
    val time: String,
    val imageUrl: String,
    val status: String,
    val color: String = "",
    val size: String = "",
    val details: String = "",
    val aiAnalysis: String = "",
    val contactPhone: String = ""
)

data class FeedState(
    val posts: List<DogPost> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
