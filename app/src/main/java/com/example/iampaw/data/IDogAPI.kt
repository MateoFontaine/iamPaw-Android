package com.example.iampaw.data

import com.example.iampaw.BuildConfig // Asegurate de importar tu BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header

interface IDogAPI {
    @GET("v1/breeds")
    suspend fun getBreeds(
        // Le pasamos el header por parámetro con un valor por defecto
        @Header("x-api-key") apiKey: String = BuildConfig.DOG_API_KEY
    ): List<DogBreed>
}