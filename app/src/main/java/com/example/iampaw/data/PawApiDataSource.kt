package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog

class PawApiDataSource : IPawDataSource {

    // ACÁ HACEMOS LA LLAMADA REAL A RETROFIT
    override suspend fun getBreeds(): List<DogBreed> {
        return try {
            RetrofitInstance.api.getBreeds()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getFeedDogs(): List<DogPost> = emptyList()
    override fun getDogDetail(id: String): DetailState = DetailState()
    override fun getMatchedDogs(): List<MatchedDog> = emptyList()
}