package com.example.iampaw

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.components.report.ReportDraft
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

    override suspend fun getDogDetail(id: String) = DetailState()

    override fun getMatchedDogs() = emptyList<MatchedDog>()

    override suspend fun getMatchCandidates(draft: ReportDraft): List<DogPost> {
        val oppositeStatus = when {
            draft.status.contains("Perdido", ignoreCase = true) -> "Encontrado"
            draft.status.contains("Encontrado", ignoreCase = true) -> "Perdido"
            else -> return emptyList()
        }
        return posts.filter { it.status.contains(oppositeStatus, ignoreCase = true) }
    }

    override suspend fun getBreeds() = emptyList<DogBreed>()
}
