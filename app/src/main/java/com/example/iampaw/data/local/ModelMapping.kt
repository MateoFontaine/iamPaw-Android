package com.example.iampaw.data.local

import com.example.iampaw.components.feed.DogPost

fun PetReportLocal.toDogPost() = DogPost(
    id = id,
    name = name,
    breed = breed,
    location = location,
    time = time,
    imageUrl = imageUrl,
    status = status
)

fun DogPost.toLocal(userId: String = "", createdAt: Long = 0L) = PetReportLocal(
    id = id,
    name = name,
    breed = breed,
    location = location,
    time = time,
    imageUrl = imageUrl,
    status = status,
    userId = userId,
    createdAt = createdAt
)

fun List<PetReportLocal>.toDogPosts() = map { it.toDogPost() }
