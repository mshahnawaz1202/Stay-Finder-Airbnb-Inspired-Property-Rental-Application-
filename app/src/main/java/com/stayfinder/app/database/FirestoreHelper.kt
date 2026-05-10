package com.stayfinder.app.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.stayfinder.app.models.Booking
import com.stayfinder.app.models.Property
import com.stayfinder.app.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirestoreHelper — centralized Firestore access layer.
 *
 * Collections:
 *  • users       — user profiles (document ID = email)
 *  • properties  — property listings
 *  • bookings    — user bookings (sub-collection under users/{email}/bookings)
 */
class FirestoreHelper {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreHelper"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_PROPERTIES = "properties"
        const val COLLECTION_BOOKINGS = "bookings"
    }

    // ─────────────────────────────────────────────────────────
    // USER OPERATIONS
    // ─────────────────────────────────────────────────────────

    /**
     * Save or update a user document in Firestore (merge so existing fields are kept).
     */
    suspend fun saveUser(user: User) {
        try {
            db.collection(COLLECTION_USERS)
                .document(user.email)
                .set(user)
                .await()
            Log.d(TAG, "User saved: ${user.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            throw e
        }
    }

    /**
     * Update specific user profile fields without overwriting the whole document.
     */
    suspend fun updateUserProfile(email: String, updates: Map<String, Any>) {
        try {
            db.collection(COLLECTION_USERS)
                .document(email)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            throw e
        }
    }

    /**
     * Real-time listener: returns a Flow that emits whenever the user document changes.
     * The Flow is cold — it starts listening when collected and stops on cancellation.
     */
    fun observeUser(email: String): Flow<User?> = callbackFlow {
        val registration: ListenerRegistration = db.collection(COLLECTION_USERS)
            .document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "User listen failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { registration.remove() }
    }

    /**
     * Callback-based sync (kept for backward compatibility with existing Activities).
     */
    fun syncUserData(email: String, onUpdate: (User?) -> Unit): ListenerRegistration {
        return db.collection(COLLECTION_USERS)
            .document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed", error)
                    onUpdate(null)
                    return@addSnapshotListener
                }
                onUpdate(snapshot?.toObject(User::class.java))
            }
    }

    // ─────────────────────────────────────────────────────────
    // PROPERTY OPERATIONS
    // ─────────────────────────────────────────────────────────

    /**
     * Add a new property listing. Returns the generated Firestore document ID.
     */
    suspend fun addProperty(property: Map<String, Any>): String {
        return try {
            val ref = db.collection(COLLECTION_PROPERTIES).add(property).await()
            Log.d(TAG, "Property added: ${ref.id}")
            ref.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding property", e)
            throw e
        }
    }

    /**
     * Real-time Flow of all properties. Emits on every Firestore update.
     */
    fun observeProperties(): Flow<List<Property>> = callbackFlow {
        val registration = db.collection(COLLECTION_PROPERTIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Properties listen failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Property::class.java)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    // ─────────────────────────────────────────────────────────
    // BOOKING OPERATIONS (sub-collection: users/{email}/bookings)
    // ─────────────────────────────────────────────────────────

    /**
     * Save a booking under the user's sub-collection.
     */
    suspend fun saveBooking(userEmail: String, booking: Booking) {
        try {
            db.collection(COLLECTION_USERS)
                .document(userEmail)
                .collection(COLLECTION_BOOKINGS)
                .document(booking.id.toString())
                .set(booking)
                .await()
            Log.d(TAG, "Booking saved for $userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving booking", e)
            throw e
        }
    }

    /**
     * Real-time Flow of a user's bookings.
     */
    fun observeBookings(userEmail: String): Flow<List<Booking>> = callbackFlow {
        val registration = db.collection(COLLECTION_USERS)
            .document(userEmail)
            .collection(COLLECTION_BOOKINGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Bookings listen failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Booking::class.java)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }
}
