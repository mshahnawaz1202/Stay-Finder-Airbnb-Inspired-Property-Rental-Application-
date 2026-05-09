package com.example.stayfinder.firebase

import com.example.stayfinder.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class ListingChange(
    val title: String?,
    val ownerUid: String?,
    val isFromMe: Boolean
)

class FirestoreListingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val listings = db.collection(COL_LISTINGS)
    private var backgroundRegistration: ListenerRegistration? = null

    fun listenAll(onUpdate: (List<Property>) -> Unit): ListenerRegistration {
        return listings.addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            val list = snap.documents.mapNotNull { docToProperty(it) }
                .sortedByDescending { it.createdAtMillis }
            onUpdate(list)
        }
    }

    fun attachBackgroundListingListener(onRemoteChange: (List<ListingChange>) -> Unit) {
        backgroundRegistration?.remove()
        backgroundRegistration = listings.addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            val myUid = FirebaseAuth.getInstance().currentUser?.uid
            val changes = snap.documentChanges.mapNotNull { change ->
                val doc = change.document
                if (doc.metadata.hasPendingWrites) return@mapNotNull null
                val owner = doc.getString(FIELD_OWNER_UID)
                val title = doc.getString(FIELD_TITLE)
                ListingChange(
                    title = title,
                    ownerUid = owner,
                    isFromMe = owner != null && owner == myUid
                )
            }
            if (changes.isNotEmpty()) onRemoteChange(changes)
        }
    }

    fun removeBackgroundListingListener() {
        backgroundRegistration?.remove()
        backgroundRegistration = null
    }

    suspend fun addListing(
        title: String,
        description: String,
        pricePerNight: Double,
        propertyType: String,
        location: String,
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        tags: List<String>
    ): String {
        val owner = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Must be signed in to host")
        val priceDisplay = "$${pricePerNight.toInt()}/night"
        val data = hashMapOf<String, Any>(
            FIELD_TITLE to title,
            FIELD_DESCRIPTION to description,
            FIELD_PRICE_PER_NIGHT to pricePerNight,
            FIELD_PRICE_DISPLAY to priceDisplay,
            FIELD_PROPERTY_TYPE to propertyType,
            FIELD_LOCATION to location,
            FIELD_IMAGE_URL to imageUrl,
            FIELD_OWNER_UID to owner,
            FIELD_RATING to 4.5,
            FIELD_GUESTS to 2,
            FIELD_BEDROOMS to 1,
            FIELD_BATHROOMS to 1,
            FIELD_LATITUDE to latitude,
            FIELD_LONGITUDE to longitude,
            FIELD_TAGS to tags,
            FIELD_CREATED_AT to FieldValue.serverTimestamp()
        )
        val ref = listings.document()
        ref.set(data).await()
        return ref.id
    }

    suspend fun queryWithFilters(
        maxPrice: Double?,
        minRating: Double?,
        locationPrefix: String?,
        tag: String?
    ): List<Property> {
        val snap = listings.get().await()
        var list = snap.documents.mapNotNull { docToProperty(it) }
        if (maxPrice != null) {
            list = list.filter { it.priceValue <= maxPrice }
        }
        if (minRating != null) {
            list = list.filter { (it.rating.toDoubleOrNull() ?: 0.0) >= minRating }
        }
        if (!locationPrefix.isNullOrBlank()) {
            val p = locationPrefix.trim().lowercase()
            list = list.filter { it.location.lowercase().contains(p) }
        }
        if (!tag.isNullOrBlank() && tag != "all") {
            val t = tag.trim().lowercase()
            list = list.filter { prop ->
                prop.tags.any { it.lowercase().contains(t) } ||
                    prop.title.lowercase().contains(t) ||
                    prop.description.lowercase().contains(t)
            }
        }
        return list.sortedByDescending { it.createdAtMillis }
    }

    companion object {
        const val COL_LISTINGS = "listings"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PRICE_PER_NIGHT = "pricePerNight"
        const val FIELD_PRICE_DISPLAY = "priceDisplay"
        const val FIELD_PROPERTY_TYPE = "propertyType"
        const val FIELD_LOCATION = "location"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_OWNER_UID = "ownerUid"
        const val FIELD_RATING = "rating"
        const val FIELD_GUESTS = "guests"
        const val FIELD_BEDROOMS = "bedrooms"
        const val FIELD_BATHROOMS = "bathrooms"
        const val FIELD_LATITUDE = "latitude"
        const val FIELD_LONGITUDE = "longitude"
        const val FIELD_TAGS = "tags"
        const val FIELD_CREATED_AT = "createdAt"

        fun docToProperty(doc: DocumentSnapshot): Property? {
            val title = doc.getString(FIELD_TITLE) ?: return null
            val priceDisplay = doc.getString(FIELD_PRICE_DISPLAY)
                ?: doc.getDouble(FIELD_PRICE_PER_NIGHT)?.let { "$${it.toInt()}/night" }
                ?: ""
            val priceValue = doc.getDouble(FIELD_PRICE_PER_NIGHT) ?: 0.0
            val createdAt = doc.getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: 0L
            val tags: ArrayList<String> = (doc.get(FIELD_TAGS) as? Iterable<*>)
                ?.mapNotNull { it as? String }
                ?.toCollection(ArrayList())
                ?: arrayListOf()
            return Property(
                id = doc.id,
                title = title,
                location = doc.getString(FIELD_LOCATION) ?: "",
                description = doc.getString(FIELD_DESCRIPTION) ?: "",
                price = priceDisplay,
                priceValue = priceValue,
                rating = doc.getDouble(FIELD_RATING)?.toString() ?: "0.0",
                guests = (doc.getLong(FIELD_GUESTS) ?: 1L).toInt(),
                bedrooms = (doc.getLong(FIELD_BEDROOMS) ?: 1L).toInt(),
                bathrooms = (doc.getLong(FIELD_BATHROOMS) ?: 1L).toInt(),
                propertyType = doc.getString(FIELD_PROPERTY_TYPE) ?: "House",
                imageUrl = doc.getString(FIELD_IMAGE_URL) ?: "",
                createdAtMillis = createdAt,
                tags = tags,
                ownerUid = doc.getString(FIELD_OWNER_UID) ?: "",
                latitude = doc.getDouble(FIELD_LATITUDE) ?: 0.0,
                longitude = doc.getDouble(FIELD_LONGITUDE) ?: 0.0
            )
        }
    }
}
