package com.example.iampaw.components.detail

data class DetailState(
    val name: String = "",
    val breed: String = "",
    val color: String = "",
    val size: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val aiAnalysis: String = "",
    val isLost: Boolean = true
)