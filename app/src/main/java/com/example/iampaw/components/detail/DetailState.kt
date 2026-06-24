package com.example.iampaw.components.detail

data class DetailState(
    val postId: String = "",
    val name: String = "",
    val breed: String = "",
    val color: String = "",
    val size: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val aiAnalysis: String = "",
    val status: String = "",
    val isLost: Boolean = true,
    val isResolved: Boolean = false,
    val isOwner: Boolean = false,
    val contactPhone: String = "",
    val isResolving: Boolean = false,
    val resolveError: String? = null
)