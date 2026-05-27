package com.example.iampaw.data

import com.example.iampaw.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header

interface IDogAPI {
    @GET("v1/breeds")
    suspend fun getBreeds(
        @Header("x-api-key") apiKey: String = BuildConfig.DOG_API_KEY
    ): List<DogBreed>
}