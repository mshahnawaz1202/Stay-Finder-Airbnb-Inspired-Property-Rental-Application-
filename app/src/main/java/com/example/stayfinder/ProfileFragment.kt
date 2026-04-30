package com.example.stayfinder

import android.annotation.SuppressLint
import android.os.Bundle
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

        // Receive data using Intent from host Activity
        val intent = activity?.intent
        var username = intent?.getStringExtra("USERNAME") ?: "Guest User"
        var email = intent?.getStringExtra("EMAIL") ?: "guest@example.com"

        // For demo fallback if not properly assigned
        if (username.isBlank()) username = "Muhammad Shah Nawaz"
        if (email.isBlank()) email = "shahnawaz@gamil.com"

        tvName?.text = username
        tvEmail?.text = email

        btnLogout?.setOnClickListener {
            val loginIntent = Intent(activity, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            activity?.finish()
        }
        
        return view
    }
}
