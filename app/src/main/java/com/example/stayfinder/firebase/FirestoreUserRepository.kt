package com.example.stayfinder.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository {

    private val users = FirebaseFirestore.getInstance().collection(COL_USERS)

    suspend fun ensureUserProfile(uid: String, email: String?, displayName: String?) {
        val doc = users.document(uid).get().await()
        if (doc.exists()) return
        val data = hashMapOf(
            FIELD_UID to uid,
            FIELD_EMAIL to (email ?: ""),
            FIELD_DISPLAY_NAME to (displayName ?: ""),
            FIELD_CREATED_AT to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        users.document(uid).set(data, SetOptions.merge()).await()
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        users.document(uid).set(
            mapOf(FIELD_FCM_TOKEN to token),
            SetOptions.merge()
        ).await()
    }

    companion object {
        const val COL_USERS = "users"
        const val FIELD_UID = "uid"
        const val FIELD_EMAIL = "email"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_FCM_TOKEN = "fcmToken"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
