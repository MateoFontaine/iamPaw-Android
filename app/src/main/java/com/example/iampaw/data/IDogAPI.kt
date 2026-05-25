package com.example.iampaw.data

import retrofit2.http.GET
import retrofit2.http.Headers

interface IDogAPI {
    @Headers("x-api-key: live_WmN7jhycGzaLP0XocFYG1HrKsC4oYmqi8E52PjzNpLKd0oevJ3aynoWLgmaknSsd")
    @GET("v1/breeds")
    suspend fun getBreeds(): List<DogBreed>
}