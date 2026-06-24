package com.example.iampaw.data.local

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost

fun PetReportLocal.toDogPost() = DogPost(
    id = id,
    name = name,
    breed = breed,
    location = location,
    time = time,
    imageUrl = imageUrl,
    status = status,
    color = color,
    size = size,
    details = details,
    aiAnalysis = aiAnalysis
)

fun DogPost.toLocal(userId: String = "", createdAt: Long = 0L) = PetReportLocal(
    id = id,
    name = name,
    breed = breed,
    color = color,
    size = size,
    details = details,
    aiAnalysis = aiAnalysis,
    location = location,
    time = time,
    imageUrl = imageUrl,
    status = status,
    userId = userId,
    createdAt = createdAt
)

fun PetReportLocal.toDetailState() = DetailState(
    name = name,
    breed = breed,
    location = location,
    imageUrl = imageUrl,
    color = color,
    size = size,
    description = details,
    aiAnalysis = aiAnalysis,
    isLost = status.contains("Perdido", ignoreCase = true)
)

fun List<PetReportLocal>.toDogPosts() = map { it.toDogPost() }
