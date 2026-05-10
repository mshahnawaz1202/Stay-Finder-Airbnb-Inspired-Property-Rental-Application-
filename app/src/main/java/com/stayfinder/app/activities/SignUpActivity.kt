package com.stayfinder.app.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stayfinder.app.R
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.database.FirestoreHelper
import com.stayfinder.app.databinding.ActivitySignupBinding
import com.stayfinder.app.models.User
import com.stayfinder.app.repository.AuthRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var db: DatabaseHelper
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreHelper: FirestoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        authRepository = AuthRepository(this)
        firestoreHelper = FirestoreHelper()

        setupPasswordStrengthWatcher()
        setupClickListeners()
    }

    // ─────────────────────────────────────────────────────────
    // PASSWORD STRENGTH (visual feedback — part of New Feature #1/2 polish)
    // ─────────────────────────────────────────────────────────
    private fun setupPasswordStrengthWatcher() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pw = s.toString()
                val strength = when {
                    pw.length >= 12 && pw.any { it.isDigit() } && pw.any { !it.isLetterOrDigit() } -> "Strong 💪"
                    pw.length >= 8 && pw.any { it.isDigit() } -> "Medium 👍"
                    pw.length >= 6 -> "Weak ⚠️"
                    else -> ""
                }
                binding.tilPassword.helperText = if (strength.isNotEmpty()) "Strength: $strength" else null
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            if (validateForm()) performSignUp()
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun performSignUp() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.btnSignup.isEnabled = false

        lifecycleScope.launch {
            try {
                val authResult = authRepository.signUp(email, password)
                if (authResult.user != null) {
                    val avatarColors = listOf(
                        "#FF385C", "#00A699", "#FC642D", "#484848", "#767676"
                    )
                    val avatarColor = avatarColors[Random().nextInt(avatarColors.size)]
                    val dateJoined = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date())
                    val role = if (binding.rbGuest.isChecked) "Guest" else "Host"

                    val user = User(
                        id = 0,
                        fullName = name,
                        email = email,
                        // NOTE: Password is NOT stored in Firestore for security.
                        // Firebase Auth manages credentials. We store an empty string.
                        password = "",
                        role = role,
                        profileBio = "",
                        phoneNumber = "",
                        dateJoined = dateJoined,
                        avatarColor = avatarColor
                    )

                    // Save to Firestore (source of truth)
                    firestoreHelper.saveUser(user)

                    // Cache to local SQLite
                    db.insertUser(name, email, password, role, dateJoined, avatarColor)

                    // Save last email for biometric feature
                    getSharedPreferences("stayfinder_prefs", MODE_PRIVATE).edit()
                        .putString("last_email", email)
                        .apply()

                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java).apply {
                        putExtra("SUCCESS_MSG", "Account created! Please sign in.")
                        putExtra("PRE_FILL_EMAIL", email)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Registration failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        var isValid = true

        if (name.length < 2) {
            binding.tilName.error = getString(R.string.error_name_empty)
            isValid = false
        } else binding.tilName.error = null

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else binding.tilEmail.error = null

        if (password.length < 8 || !password.any { it.isDigit() }) {
            binding.tilPassword.error = getString(R.string.error_password_complexity)
            isValid = false
        } else binding.tilPassword.error = null

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_match)
            isValid = false
        } else binding.tilConfirmPassword.error = null

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, getString(R.string.error_terms), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }
}
