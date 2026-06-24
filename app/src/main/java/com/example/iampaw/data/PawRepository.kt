package com.example.iampaw.data

import android.util.Log
import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.ContactPhoneFormatter
import com.example.iampaw.data.local.ContactPhoneStorage
import com.example.iampaw.data.local.IPawDao
import com.example.iampaw.data.local.PetReportLocal
import com.example.iampaw.data.local.ReportImageStorage
import com.example.iampaw.data.local.toDetailState
import com.example.iampaw.data.local.toDogPosts
import com.example.iampaw.data.local.toLocal
import com.example.iampaw.data.remote.FirestoreReportDataSource
import com.example.iampaw.data.remote.FirestoreUserDataSource
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
    private val userDataSource: FirestoreUserDataSource,
    private val contactPhoneStorage: ContactPhoneStorage,
    private val firebaseAuth: FirebaseAuth,
    private val reportImageStorage: ReportImageStorage
) : IPawRepository {

    override fun observeFeed(): Flow<List<DogPost>> =
        pawDao.observeAll().map { reports ->
            reports
                .filter { !it.status.contains("Resuelto", ignoreCase = true) }
                .toDogPosts()
        }

    override suspend fun refreshFeedIfEmpty() {
        if (pawDao.count() == 0) {
            Log.d(TAG, "Feed vacío — esperando reportes del usuario")
        }
    }

    override suspend fun saveReport(report: DogPost): Result<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid.orEmpty()
            val contactPhone = if (userId.isNotEmpty()) getContactPhoneForUser(userId) else ""
            val persistedImageUrl = reportImageStorage.persistReportImage(report.imageUrl, report.id)
            val local = report.copy(imageUrl = persistedImageUrl).toLocal(
                userId = userId,
                createdAt = System.currentTimeMillis(),
                contactPhone = contactPhone
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

    override suspend fun getDogDetail(id: String): DetailState {
        val report = pawDao.getById(id) ?: return DetailState()
        val currentUserId = firebaseAuth.currentUser?.uid.orEmpty()
        val isOwner = currentUserId.isNotEmpty() && report.userId == currentUserId
        val contactPhone = resolveContactPhone(report)
        return report.toDetailState(isOwner = isOwner).copy(contactPhone = contactPhone)
    }

    override suspend fun markReportResolved(reportId: String): Result<Unit> {
        return try {
            val report = pawDao.getById(reportId)
                ?: return Result.failure(IllegalStateException("Reporte no encontrado"))
            val currentUserId = firebaseAuth.currentUser?.uid.orEmpty()
            if (currentUserId.isEmpty() || report.userId != currentUserId) {
                return Result.failure(IllegalStateException("Solo podés marcar tus publicaciones"))
            }
            if (report.status.contains("Resuelto", ignoreCase = true)) {
                return Result.success(Unit)
            }
            val updated = report.copy(status = "Resuelto", time = "Reunido")
            pawDao.insertAll(listOf(updated))
            try {
                firestoreDataSource.saveReport(updated)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore update failed, resolved locally: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark report resolved: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getMatchedDogs(): List<MatchedDog> = emptyList()

    override suspend fun getMatchCandidates(draft: ReportDraft): List<DogPost> {
        val oppositeStatus = when {
            draft.status.contains("Perdido", ignoreCase = true) -> "Encontrado"
            draft.status.contains("Encontrado", ignoreCase = true) -> "Perdido"
            else -> return emptyList()
        }

        return pawDao.getAll()
            .toDogPosts()
            .filter { post ->
                post.status.contains(oppositeStatus, ignoreCase = true) &&
                    !post.status.contains("Resuelto", ignoreCase = true)
            }
    }

    override suspend fun getBreeds(): List<DogBreed> = apiDataSource.getBreeds()

    override suspend fun getUserContactPhone(): String {
        val userId = firebaseAuth.currentUser?.uid.orEmpty()
        if (userId.isEmpty()) return ""
        val phone = getContactPhoneForUser(userId)
        if (phone.isNotBlank()) {
            backfillContactPhoneOnReports(userId, phone)
        }
        return phone
    }

    override suspend fun saveUserContactPhone(phone: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(IllegalStateException("Tenés que iniciar sesión"))
            val normalized = ContactPhoneFormatter.normalizeForWhatsApp(phone)
                ?: return Result.failure(IllegalStateException("Teléfono inválido. Usá formato 11XXXXXXXX o 54911…"))
            contactPhoneStorage.save(user.uid, normalized)
            try {
                userDataSource.saveContactPhone(
                    userId = user.uid,
                    phone = normalized,
                    displayName = user.displayName.orEmpty(),
                    email = user.email.orEmpty()
                )
            } catch (e: Exception) {
                Log.w(TAG, "Firestore users/ save failed, phone kept locally: ${e.message}")
            }
            backfillContactPhoneOnReports(user.uid, normalized)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getContactPhoneForUser(userId: String): String {
        val remote = try {
            userDataSource.getContactPhone(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore users/ read failed: ${e.message}")
            ""
        }
        if (remote.isNotBlank()) {
            contactPhoneStorage.save(userId, remote)
            return remote
        }
        return contactPhoneStorage.get(userId)
    }

    private suspend fun resolveContactPhone(report: PetReportLocal): String {
        if (report.contactPhone.isNotBlank()) return report.contactPhone
        if (report.userId.isBlank()) return ""
        return getContactPhoneForUser(report.userId)
    }

    private suspend fun backfillContactPhoneOnReports(userId: String, phone: String) {
        val reports = pawDao.getByUserId(userId)
            .filter { it.contactPhone != phone }
        if (reports.isEmpty()) return
        val updated = reports.map { it.copy(contactPhone = phone) }
        pawDao.insertAll(updated)
        updated.forEach { report ->
            try {
                firestoreDataSource.saveReport(report)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore backfill phone failed for ${report.id}: ${e.message}")
            }
        }
        Log.d(TAG, "Backfill teléfono en ${updated.size} reporte(s) de userId=$userId")
    }
}
