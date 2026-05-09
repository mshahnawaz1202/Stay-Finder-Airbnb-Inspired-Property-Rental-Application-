package com.example.stayfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stayfinder.auth.AuthRouterActivity
import com.example.stayfinder.firebase.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private val authRepository = AuthRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnHost = view.findViewById<MaterialButton>(R.id.btnEditProfile)

        val user = FirebaseAuth.getInstance().currentUser
        tvName.text = user?.displayName ?: "Guest"
        tvEmail.text = user?.email ?: ""

        btnHost.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HostFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            GoogleSignIn.getClient(requireContext(), gso).signOut()
            authRepository.signOut()
            startActivity(Intent(requireContext(), AuthRouterActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }

        return view
    }
}
