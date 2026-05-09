package com.example.stayfinder.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stayfinder.MainActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Auth flow entry: sends signed-in users to [MainActivity], others to [AuthActivity].
 */
class AuthRouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val next = if (FirebaseAuth.getInstance().currentUser != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }
        startActivity(next)
        finish()
    }
}
