package com.example.iampaw.data.remote

import com.example.iampaw.data.local.PetReportLocal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_REPORTS = "reports"

@Singleton
class FirestoreReportDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun saveReport(report: PetReportLocal) {
        firestore.collection(COLLECTION_REPORTS)
            .document(report.id)
            .set(report.toFirestoreMap())
            .await()
    }

    suspend fun fetchAllReports(): List<PetReportLocal> {
        val snapshot = firestore.collection(COLLECTION_REPORTS).get().await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            PetReportLocal(
                id = id,
                name = doc.getString("name") ?: "",
                breed = doc.getString("breed") ?: "",
                color = doc.getString("color") ?: "",
                size = doc.getString("size") ?: "",
                details = doc.getString("details") ?: "",
                aiAnalysis = doc.getString("aiAnalysis") ?: "",
                location = doc.getString("location") ?: "",
                time = doc.getString("time") ?: "",
                imageUrl = doc.getString("imageUrl") ?: "",
                status = doc.getString("status") ?: "",
                userId = doc.getString("userId") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
    }

    private fun PetReportLocal.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "breed" to breed,
        "color" to color,
        "size" to size,
        "details" to details,
        "aiAnalysis" to aiAnalysis,
        "location" to location,
        "time" to time,
        "imageUrl" to imageUrl,
        "status" to status,
        "userId" to userId,
        "createdAt" to createdAt
    )
}
