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
    aiAnalysis = aiAnalysis,
    contactPhone = contactPhone
)

fun DogPost.toLocal(userId: String = "", createdAt: Long = 0L, contactPhone: String = "") = PetReportLocal(
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
    contactPhone = contactPhone,
    createdAt = createdAt
)

fun PetReportLocal.toDetailState(isOwner: Boolean = false) = DetailState(
    postId = id,
    name = name,
    breed = breed,
    location = location,
    imageUrl = imageUrl,
    color = color,
    size = size,
    description = details,
    aiAnalysis = aiAnalysis,
    status = status,
    contactPhone = contactPhone,
    isLost = status.contains("Perdido", ignoreCase = true),
    isResolved = status.contains("Resuelto", ignoreCase = true),
    isOwner = isOwner
)

fun List<PetReportLocal>.toDogPosts() = map { it.toDogPost() }
