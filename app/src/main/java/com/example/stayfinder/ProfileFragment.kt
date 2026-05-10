package com.example.stayfinder

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        
        // Find TextViews (Need to assign IDs to them in activity_profile.xml)
        // I will add IDs `tvProfileName` and `tvProfileEmail` in activity_profile.xml
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val authManager = FirebaseAuthManager(requireContext())
        val currentUser = authManager.getCurrentUser()

        var username = "Guest User"
        var email = "guest@example.com"

        if (currentUser != null) {
            username = currentUser.displayName ?: "User"
            email = currentUser.email ?: "guest@example.com"
        }

        tvName?.text = username
        tvEmail?.text = email

        val btnToggleTheme = view.findViewById<Button>(R.id.btnToggleTheme)
        btnToggleTheme?.setOnClickListener {
            val sharedPrefs = requireActivity().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            val isDark = sharedPrefs.getBoolean("is_dark", false)
            if (isDark) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
                sharedPrefs.edit().putBoolean("is_dark", false).apply()
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                sharedPrefs.edit().putBoolean("is_dark", true).apply()
            }
        }

        btnLogout?.setOnClickListener {
            authManager.logout()
            val loginIntent = Intent(activity, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            activity?.finish()
        }
        
        return view
    }
}
