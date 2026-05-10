package com.stayfinder.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.stayfinder.app.R
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.database.FirestoreHelper
import com.stayfinder.app.databinding.ActivityLoginBinding
import com.stayfinder.app.models.User
import com.stayfinder.app.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: DatabaseHelper
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreHelper: FirestoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        authRepository = AuthRepository(this)
        firestoreHelper = FirestoreHelper()

        // Pre-fill after sign-up
        intent.getStringExtra("SUCCESS_MSG")?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        intent.getStringExtra("PRE_FILL_EMAIL")?.let {
            binding.etEmail.setText(it)
        }

        setupClickListeners()
        setupBiometricLogin()          // New Feature #1
    }

    private fun setupClickListeners() {
        // ── Email / Password Login ──────────────────────────
        binding.btnLogin.setOnClickListener {
            if (validateInput()) performEmailLogin()
        }

        // ── Sign-Up navigation ──────────────────────────────
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // ── Google Sign-In ──────────────────────────────────
        binding.btnGoogle.setOnClickListener {
            performGoogleLogin()
        }

        // ── Social placeholders ─────────────────────────────
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook login coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnApple.setOnClickListener {
            Toast.makeText(this, "Apple login coming soon", Toast.LENGTH_SHORT).show()
        }

        // ── Forgot Password (New Feature #2) ─────────────────
        binding.tvForgotPassword.setOnClickListener {
            showPasswordResetDialog()
        }
    }

    // ─────────────────────────────────────────────────────────
    // EMAIL LOGIN
    // ─────────────────────────────────────────────────────────
    private fun performEmailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        setLoading(true)
        lifecycleScope.launch {
            try {
                val authResult = authRepository.signIn(email, password)
                if (authResult.user != null) {
                    resolveAndNavigate(email, password)
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Email login error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // GOOGLE LOGIN
    // ─────────────────────────────────────────────────────────
    private fun performGoogleLogin() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val webClientId = getString(R.string.default_web_client_id)
                authRepository.signInWithGoogle(webClientId)
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val email = currentUser.email ?: ""
                    firestoreHelper.syncUserData(email) { user ->
                        if (user != null) {
                            navigateToMain(user)
                        } else {
                            // New Google user — create a minimal profile
                            lifecycleScope.launch {
                                createGoogleUserProfile(currentUser.displayName ?: "User", email)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Google login error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Google Sign-In failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun createGoogleUserProfile(name: String, email: String) {
        val avatarColors = listOf("#FF385C", "#00A699", "#FC642D", "#484848", "#767676")
        val avatarColor = avatarColors.random()
        val user = User(
            id = 0,
            fullName = name,
            email = email,
            password = "",   // Google users have no local password
            role = "Guest",
            profileBio = "",
            phoneNumber = "",
            dateJoined = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date()),
            avatarColor = avatarColor
        )
        firestoreHelper.saveUser(user)
        db.insertUser(name, email, "", "Guest", user.dateJoined, avatarColor)
        navigateToMain(user)
    }

    // ─────────────────────────────────────────────────────────
    // NEW FEATURE #2 — PASSWORD RESET
    // ─────────────────────────────────────────────────────────
    private fun showPasswordResetDialog() {
        val emailField = TextInputEditText(this).apply {
            hint = "Enter your email"
            setPadding(48, 32, 48, 32)
        }
        // Pre-fill with whatever is already typed
        val currentEmail = binding.etEmail.text.toString().trim()
        if (currentEmail.isNotEmpty()) emailField.setText(currentEmail)

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We'll send a password reset link to your email.")
            .setView(emailField)
            .setPositiveButton("Send Link") { _, _ ->
                val email = emailField.text.toString().trim()
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                sendPasswordReset(email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordReset(email: String) {
        lifecycleScope.launch {
            try {
                authRepository.sendPasswordResetEmail(email)
                Toast.makeText(
                    this@LoginActivity,
                    "Password reset email sent to $email",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Password reset error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Failed to send reset email: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // NEW FEATURE #1 — BIOMETRIC / FINGERPRINT LOGIN
    // ─────────────────────────────────────────────────────────
    private fun setupBiometricLogin() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        // Show biometric button only if hardware is available and enrolled
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnBiometric.visibility = View.VISIBLE
            binding.btnBiometric.setOnClickListener { showBiometricPrompt() }
        } else {
            binding.btnBiometric.visibility = View.GONE
        }
    }

    private fun showBiometricPrompt() {
        // Retrieve the last-logged-in email from shared prefs (saved on successful login)
        val prefs = getSharedPreferences("stayfinder_prefs", MODE_PRIVATE)
        val savedEmail = prefs.getString("last_email", null)

        if (savedEmail == null) {
            Toast.makeText(
                this,
                "Please login with email first to enable biometric.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Sign in the saved user from Firebase (session should still be valid)
                val currentFirebaseUser = authRepository.getCurrentUser()
                if (currentFirebaseUser != null) {
                    // Session still alive — go directly to main
                    val user = db.getUserByEmail(savedEmail)
                    if (user != null) navigateToMain(user)
                    else {
                        firestoreHelper.syncUserData(savedEmail) { u ->
                            u?.let { navigateToMain(it) }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Session expired. Please login with email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Biometric error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    this@LoginActivity,
                    "Biometric authentication failed. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or face to sign in")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(promptInfo)
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Check local SQLite cache first, then Firestore if not found.
     * Saves the email to prefs so biometric can use it later.
     */
    private fun resolveAndNavigate(email: String, password: String) {
        // Save last logged-in email for biometric feature
        getSharedPreferences("stayfinder_prefs", MODE_PRIVATE).edit()
            .putString("last_email", email)
            .apply()

        val localUser = db.getUserByEmail(email)
        if (localUser != null) {
            navigateToMain(localUser)
        } else {
            firestoreHelper.syncUserData(email) { firestoreUser ->
                if (firestoreUser != null) {
                    db.insertUser(
                        firestoreUser.fullName, firestoreUser.email,
                        firestoreUser.password, firestoreUser.role,
                        firestoreUser.dateJoined, firestoreUser.avatarColor
                    )
                    val cachedUser = db.getUserByEmail(email)
                    navigateToMain(cachedUser ?: firestoreUser)
                } else {
                    Toast.makeText(
                        this,
                        "User profile not found. Please sign up.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToMain(user: User) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", user.id)
            putExtra("USER_NAME", user.fullName)
            putExtra("USER_EMAIL", user.email)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        val password = binding.etPassword.text.toString().trim()
        if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.error_invalid_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        return isValid
    }
}
