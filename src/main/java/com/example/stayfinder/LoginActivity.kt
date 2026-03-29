package com.example.stayfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.etLoginUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etLoginEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("USERNAME", username)
                    putExtra("EMAIL", email)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter username and email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
