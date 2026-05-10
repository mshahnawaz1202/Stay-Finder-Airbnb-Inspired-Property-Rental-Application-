package com.stayfinder.app.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository — handles all Firebase Authentication operations.
 * All network/auth operations are suspend functions (coroutine-safe).
 */
class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    /** Returns the currently signed-in Firebase user, or null. */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /** Returns true if a user is already signed in. */
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    /**
     * Sign up with email + password.
     * @throws Exception on failure (e.g., email already in use).
     */
    suspend fun signUp(email: String, pass: String) =
        auth.createUserWithEmailAndPassword(email, pass).await()

    /**
     * Sign in with email + password.
     * @throws Exception on failure (e.g., wrong credentials).
     */
    suspend fun signIn(email: String, pass: String) =
        auth.signInWithEmailAndPassword(email, pass).await()

    /**
     * Sign out current user and clear credential state.
     */
    suspend fun logout() {
        auth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    /**
     * New Feature #2: Send a password reset email.
     * @throws Exception if email is not registered.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Google Sign-In using Credential Manager.
     * Uses the web_client_id from strings.xml (must be configured in Firebase console).
     */
    suspend fun signInWithGoogle(webClientId: String) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val googleIdToken = GoogleIdTokenCredential
            .createFrom(result.credential.data)
            .idToken

        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        auth.signInWithCredential(firebaseCredential).await()
    }

    /**
     * Re-authenticate current user (needed before sensitive operations like
     * changing email/password).
     */
    suspend fun reAuthenticate(email: String, password: String) {
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(email, password)
        auth.currentUser?.reauthenticate(credential)?.await()
    }
}
