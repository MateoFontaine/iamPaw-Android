package com.example.iampaw.domain

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.data.DogBreed
import kotlinx.coroutines.flow.Flow

interface IPawRepository {
    fun observeFeed(searchQuery: String = ""): Flow<List<DogPost>>
    suspend fun refreshFeedIfEmpty()
    fun getDogDetail(id: String): DetailState
    fun getMatchedDogs(): List<MatchedDog>
    suspend fun getBreeds(): List<DogBreed>
}
