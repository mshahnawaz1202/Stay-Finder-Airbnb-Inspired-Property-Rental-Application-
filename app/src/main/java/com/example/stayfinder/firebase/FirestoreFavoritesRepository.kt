package com.example.stayfinder.firebase

import com.example.stayfinder.Property
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class FavoriteFirestoreItem(
    val listingId: String,
    val note: String,
    val title: String,
    val price: String,
    val location: String,
    val imageUrl: String,
    val rating: String
)

data class FavoriteRemoteChange(
    val title: String?,
    val isFromMe: Boolean
)

class FirestoreFavoritesRepository {

    private val favorites by lazy { FirebaseFirestore.getInstance().collection(COL_FAVORITES) }
    private var backgroundRegistration: ListenerRegistration? = null

    fun docId(userId: String, listingId: String) = "${userId}_$listingId"

    suspend fun setFavorite(userId: String, property: Property, note: String = "") {
        val id = docId(userId, property.id)
        val data = hashMapOf<String, Any>(
            FIELD_USER_ID to userId,
            FIELD_LISTING_ID to property.id,
            FIELD_NOTE to note,
            FIELD_TITLE to property.title,
            FIELD_PRICE to property.price,
            FIELD_LOCATION to property.location,
            FIELD_IMAGE_URL to property.imageUrl,
            FIELD_RATING to property.rating,
            FIELD_UPDATED_AT to FieldValue.serverTimestamp()
        )
        favorites.document(id).set(data, SetOptions.merge()).await()
    }

    suspend fun removeFavorite(userId: String, listingId: String) {
        favorites.document(docId(userId, listingId)).delete().await()
    }

    suspend fun isFavorite(userId: String, listingId: String): Boolean {
        val doc = favorites.document(docId(userId, listingId)).get().await()
        return doc.exists()
    }

    fun listenUserFavorites(
        userId: String,
        onUpdate: (List<FavoriteFirestoreItem>) -> Unit
    ): ListenerRegistration {
        return favorites.whereEqualTo(FIELD_USER_ID, userId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val items = snap.documents.mapNotNull { doc ->
                    FavoriteFirestoreItem(
                        listingId = doc.getString(FIELD_LISTING_ID) ?: return@mapNotNull null,
                        note = doc.getString(FIELD_NOTE) ?: "",
                        title = doc.getString(FIELD_TITLE) ?: "",
                        price = doc.getString(FIELD_PRICE) ?: "",
                        location = doc.getString(FIELD_LOCATION) ?: "",
                        imageUrl = doc.getString(FIELD_IMAGE_URL) ?: "",
                        rating = doc.getString(FIELD_RATING) ?: ""
                    )
                }
                onUpdate(items)
            }
    }

    fun attachBackgroundFavoriteListener(
        userId: String,
        onRemoteChange: (List<FavoriteRemoteChange>) -> Unit
    ) {
        backgroundRegistration?.remove()
        backgroundRegistration = favorites.whereEqualTo(FIELD_USER_ID, userId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val changes = snap.documentChanges.mapNotNull { change ->
                    val doc = change.document
                    if (doc.metadata.hasPendingWrites) return@mapNotNull null
                    FavoriteRemoteChange(
                        title = doc.getString(FIELD_TITLE),
                        isFromMe = false
                    )
                }
                if (changes.isNotEmpty()) onRemoteChange(changes)
            }
    }

    fun removeBackgroundFavoriteListener() {
        backgroundRegistration?.remove()
        backgroundRegistration = null
    }

    companion object {
        const val COL_FAVORITES = "favorites"
        const val FIELD_USER_ID = "userId"
        const val FIELD_LISTING_ID = "listingId"
        const val FIELD_NOTE = "note"
        const val FIELD_TITLE = "title"
        const val FIELD_PRICE = "price"
        const val FIELD_LOCATION = "location"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_RATING = "rating"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
