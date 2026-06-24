package com.example.iampaw.data.DI

import android.content.Context
import com.example.iampaw.data.IDogAPI
import com.example.iampaw.data.PawRepository
import com.example.iampaw.data.local.IPawDao
import com.example.iampaw.data.local.PawDatabase
import com.example.iampaw.domain.IPawRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.thedogapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideDogAPI(retrofit: Retrofit): IDogAPI = retrofit.create(IDogAPI::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providePawDatabase(@ApplicationContext context: Context): PawDatabase =
        PawDatabase.getInstance(context)

    @Provides
    fun providePawDao(database: PawDatabase): IPawDao = database.pawDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPawRepository(pawRepository: PawRepository): IPawRepository
}
