package com.example.iampaw

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.data.DogBreed
import com.example.iampaw.domain.IPawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

class FakePawRepository(
    private val posts: List<DogPost> = emptyList(),
    private val shouldFailSync: Boolean = false
) : IPawRepository {

    override fun observeFeed(): Flow<List<DogPost>> = flowOf(posts)

    override suspend fun refreshFeedIfEmpty() { /* no-op */ }

    override suspend fun syncReportsFromFirestore() {
        if (shouldFailSync) throw IOException("Error simulado")
    }

    override suspend fun saveReport(report: DogPost) = Result.success(Unit)

    override fun getDogDetail(id: String) = DetailState()

    override fun getMatchedDogs() = emptyList<MatchedDog>()

    override suspend fun getBreeds() = emptyList<DogBreed>()
}
