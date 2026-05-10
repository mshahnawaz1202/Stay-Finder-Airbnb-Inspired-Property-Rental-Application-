package com.stayfinder.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.stayfinder.app.R
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.ActivitySplashBinding
import com.stayfinder.app.notifications.StayFinderMessagingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create FCM notification channels as early as possible
        StayFinderMessagingService.createNotificationChannels(this)

        // Simple entrance animation
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        lifecycleScope.launch {
            delay(2000L) // Show splash for 2 seconds
            decideNavigation()
        }
    }

    private fun decideNavigation() {
        val prefs = getSharedPreferences("stayfinder_prefs", MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean("onboarding_done", false)

        if (!hasSeenOnboarding) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: ""
            val db = DatabaseHelper.getInstance(this)
            val localUser = db.getUserByEmail(email)
            
            if (localUser != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("USER_ID", localUser.id)
                    putExtra("USER_NAME", localUser.fullName)
                    putExtra("USER_EMAIL", localUser.email)
                }
                startActivity(intent)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
