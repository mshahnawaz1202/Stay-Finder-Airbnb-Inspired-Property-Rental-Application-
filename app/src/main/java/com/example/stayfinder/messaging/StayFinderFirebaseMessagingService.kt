package com.example.stayfinder.messaging

import com.example.stayfinder.firebase.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StayFinderFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepo = FirestoreUserRepository()

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            userRepo.updateFcmToken(uid, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        AppNotificationCenter.showFromFcm(
            applicationContext,
            message.notification?.title ?: message.data["title"],
            message.notification?.body ?: message.data["body"]
        )
    }
}
