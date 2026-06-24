package com.example.iampaw.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_USERS = "users"

@Singleton
class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getContactPhone(userId: String): String {
        if (userId.isBlank()) return ""
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            doc.getString("contactPhone").orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun saveContactPhone(userId: String, phone: String, displayName: String = "", email: String = "") {
        if (userId.isBlank()) return
        val data = buildMap<String, Any> {
            put("contactPhone", phone)
            if (displayName.isNotBlank()) put("displayName", displayName)
            if (email.isNotBlank()) put("email", email)
            put("updatedAt", System.currentTimeMillis())
        }
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(data, SetOptions.merge())
            .await()
    }
}
