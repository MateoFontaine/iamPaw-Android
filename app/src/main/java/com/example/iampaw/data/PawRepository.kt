package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.domain.IPawRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PawRepository @Inject constructor(
    private val mockDataSource: PawMockDataSource,
    private val apiDataSource: PawApiDataSource
) : IPawRepository {

    override fun getFeedDogs(): List<DogPost> = mockDataSource.getFeedDogs()

    override fun getDogDetail(id: String): DetailState = mockDataSource.getDogDetail(id)

    override fun getMatchedDogs(): List<MatchedDog> = mockDataSource.getMatchedDogs()

    override suspend fun getBreeds(): List<DogBreed> = apiDataSource.getBreeds()
}
