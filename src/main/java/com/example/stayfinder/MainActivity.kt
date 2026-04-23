package com.example.stayfinder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Load default fragment ──────────────────────────────────────
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment())
                .commit()
        }

        // ── Bottom Navigation ──────────────────────────────────────────
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home      -> HomeFragment()
                R.id.nav_favorites -> FavoritesFragment()
                R.id.nav_map       -> MapFragment()
                R.id.nav_host      -> HostFragment()
                R.id.nav_profile   -> ProfileFragment()
                else               -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit()
            true
        }
    }
}