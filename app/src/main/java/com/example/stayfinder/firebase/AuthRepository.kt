package com.example.stayfinder.firebase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepo: FirestoreUserRepository = FirestoreUserRepository()
) {

    val currentUser get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        syncProfile()
    }

    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<Unit> =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await()
            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            auth.currentUser!!.updateProfile(profile).await()
            syncProfile()
        }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Unit> = runCatching {
        val token = account.idToken ?: error("Missing Google ID token")
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).await()
        syncProfile()
    }

    private suspend fun syncProfile() {
        val u = auth.currentUser ?: return
        userRepo.ensureUserProfile(
            u.uid,
            u.email,
            u.displayName
        )
    }

    fun signOut() {
        auth.signOut()
    }
}
