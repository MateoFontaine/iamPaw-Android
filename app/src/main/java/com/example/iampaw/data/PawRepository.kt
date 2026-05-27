package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog

class PawRepository(
    private val mockDataSource: IPawDataSource = PawMockDataSource(),
    private val apiDataSource: IPawDataSource = PawApiDataSource()
) {


    fun getFeedDogs(): List<DogPost> = mockDataSource.getFeedDogs()
    fun getDogDetail(id: String): DetailState = mockDataSource.getDogDetail(id)
    fun getMatchedDogs(): List<MatchedDog> = mockDataSource.getMatchedDogs()

    suspend fun getBreeds(): List<DogBreed> = apiDataSource.getBreeds()
}