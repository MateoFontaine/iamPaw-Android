package com.example.iampaw.data

import android.util.Log
import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.local.IPawDao
import com.example.iampaw.data.local.ReportImageStorage
import com.example.iampaw.data.local.toDetailState
import com.example.iampaw.data.local.toDogPosts
import com.example.iampaw.data.local.toLocal
import com.example.iampaw.data.remote.FirestoreReportDataSource
import com.example.iampaw.domain.IPawRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PawRepository"

@Singleton
class PawRepository @Inject constructor(
    private val pawDao: IPawDao,
    private val apiDataSource: PawApiDataSource,
    private val firestoreDataSource: FirestoreReportDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val reportImageStorage: ReportImageStorage
) : IPawRepository {

    override fun observeFeed(): Flow<List<DogPost>> =
        pawDao.observeAll().map { reports -> reports.toDogPosts() }

    override suspend fun refreshFeedIfEmpty() {
        if (pawDao.count() == 0) {
            Log.d(TAG, "Feed vacío — esperando reportes del usuario")
        }
    }

    override suspend fun saveReport(report: DogPost): Result<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid.orEmpty()
            val persistedImageUrl = reportImageStorage.persistReportImage(report.imageUrl, report.id)
            val local = report.copy(imageUrl = persistedImageUrl).toLocal(
                userId = userId,
                createdAt = System.currentTimeMillis()
            )
            pawDao.insertAll(listOf(local))
            try {
                firestoreDataSource.saveReport(local)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore upload failed, report saved locally: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save report locally: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun syncReportsFromFirestore() {
        try {
            val remoteReports = firestoreDataSource.fetchAllReports()
            val remoteIds = remoteReports.map { it.id }.toSet()

            if (remoteReports.isNotEmpty()) {
                pawDao.insertAll(remoteReports)
            }

            val idsToDelete = pawDao.getSyncedReportIds().filter { it !in remoteIds }
            if (idsToDelete.isNotEmpty()) {
                idsToDelete.forEach { reportImageStorage.deleteReportImage(it) }
                pawDao.deleteByIds(idsToDelete)
                Log.d(TAG, "Removed ${idsToDelete.size} local report(s) no longer in Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync failed, using local data: ${e.message}")
        }
    }

    override suspend fun getDogDetail(id: String): DetailState =
        pawDao.getById(id)?.toDetailState() ?: DetailState()

    override fun getMatchedDogs(): List<MatchedDog> = emptyList()

    override suspend fun getMatchCandidates(draft: ReportDraft): List<DogPost> {
        val oppositeStatus = when {
            draft.status.contains("Perdido", ignoreCase = true) -> "Encontrado"
            draft.status.contains("Encontrado", ignoreCase = true) -> "Perdido"
            else -> return emptyList()
        }

        return pawDao.getAll()
            .toDogPosts()
            .filter { post -> post.status.contains(oppositeStatus, ignoreCase = true) }
    }

    override suspend fun getBreeds(): List<DogBreed> = apiDataSource.getBreeds()
}
