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

fun DogPost.toLocal() = PetReportLocal(
    id = id,
    name = name,
    breed = breed,
    location = location,
    time = time,
    imageUrl = imageUrl,
    status = status
)

fun List<PetReportLocal>.toDogPosts() = map { it.toDogPost() }
