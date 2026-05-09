package com.example.stayfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stayfinder.auth.AuthRouterActivity

/**
 * Legacy entry retained for compatibility; routing is handled by [AuthRouterActivity].
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, AuthRouterActivity::class.java))
        finish()
    }
}
