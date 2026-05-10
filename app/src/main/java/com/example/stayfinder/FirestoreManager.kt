package com.example.stayfinder

import android.util.Log
import com.example.stayfinder.models.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    init {
        // Enable offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    fun addBooking(userId: String, booking: Booking, onComplete: (Boolean) -> Unit) {
        val userBookingsRef = db.collection("users").document(userId).collection("bookings").document()
        booking.id = userBookingsRef.id
        userBookingsRef.set(booking)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                Log.e("FirestoreManager", "Error adding booking", it)
                onComplete(false)
            }
    }

    fun updateBooking(userId: String, booking: Booking, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("bookings").document(booking.id)
            .set(booking)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                Log.e("FirestoreManager", "Error updating booking", it)
                onComplete(false)
            }
    }

    fun deleteBooking(userId: String, bookingId: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("bookings").document(bookingId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                Log.e("FirestoreManager", "Error deleting booking", it)
                onComplete(false)
            }
    }

    fun getBookingsRef(userId: String): com.google.firebase.firestore.CollectionReference {
        return db.collection("users").document(userId).collection("bookings")
    }
}
