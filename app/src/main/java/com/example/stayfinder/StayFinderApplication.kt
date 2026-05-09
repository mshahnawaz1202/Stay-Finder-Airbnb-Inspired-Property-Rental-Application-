package com.example.stayfinder

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.stayfinder.firebase.FirestoreFavoritesRepository
import com.example.stayfinder.firebase.FirestoreListingRepository
import com.example.stayfinder.firebase.FirestoreUserRepository
import com.example.stayfinder.messaging.AppNotificationCenter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StayFinderApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val listingRepo = FirestoreListingRepository()
    private val favoritesRepo = FirestoreFavoritesRepository()
    private val userRepo = FirestoreUserRepository()

    private var listingsListenerStarted = false
    private var favoritesListenerStarted = false
    private var listingFirstSnapshot = true
    private var favoritesFirstSnapshot = true

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                startFirestoreBackgroundListeners()
                syncFcmTokenIfSignedIn()
            }

            override fun onStop(owner: LifecycleOwner) {
                listingRepo.removeBackgroundListingListener()
                favoritesRepo.removeBackgroundFavoriteListener()
                listingsListenerStarted = false
                favoritesListenerStarted = false
                listingFirstSnapshot = true
                favoritesFirstSnapshot = true
            }
        })

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                syncFcmTokenIfSignedIn()
            }
        }
    }

    private fun syncFcmTokenIfSignedIn() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            appScope.launch(Dispatchers.IO) {
                userRepo.updateFcmToken(user.uid, token)
            }
        }
    }

    private fun startFirestoreBackgroundListeners() {
        if (listingsListenerStarted) return
        listingsListenerStarted = true
        listingRepo.attachBackgroundListingListener { changes ->
            if (listingFirstSnapshot) {
                listingFirstSnapshot = false
                return@attachBackgroundListingListener
            }
            for (c in changes) {
                if (c.isFromMe) continue
                AppNotificationCenter.showNewListing(
                    this,
                    c.title ?: "New listing"
                )
            }
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && !favoritesListenerStarted) {
            favoritesListenerStarted = true
            favoritesRepo.attachBackgroundFavoriteListener(uid) { changes ->
                if (favoritesFirstSnapshot) {
                    favoritesFirstSnapshot = false
                    return@attachBackgroundFavoriteListener
                }
                for (c in changes) {
                    if (c.isFromMe) continue
                    AppNotificationCenter.showFavoriteUpdated(
                        this,
                        c.title ?: "Favorite"
                    )
                }
            }
        }
    }
}
