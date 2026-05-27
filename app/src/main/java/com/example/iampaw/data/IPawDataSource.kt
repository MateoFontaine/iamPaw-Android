package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog

interface IPawDataSource {
    fun getFeedDogs(): List<DogPost>
    fun getDogDetail(id: String): DetailState
    fun getMatchedDogs(): List<MatchedDog>

    suspend fun getBreeds(): List<DogBreed>
}