package com.example.iampaw.domain

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.data.DogBreed

interface IPawRepository {
    fun getFeedDogs(): List<DogPost>
    fun getDogDetail(id: String): DetailState
    fun getMatchedDogs(): List<MatchedDog>
    suspend fun getBreeds(): List<DogBreed>
}
