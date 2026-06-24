package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import javax.inject.Inject

class PawApiDataSource @Inject constructor(
    private val api: IDogAPI
) : IPawDataSource {

    override suspend fun getBreeds(): List<DogBreed> {
        return try {
            api.getBreeds()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getFeedDogs(): List<DogPost> = emptyList()

    override fun getDogDetail(id: String): DetailState = DetailState()

    override fun getMatchedDogs(): List<MatchedDog> = emptyList()
}
