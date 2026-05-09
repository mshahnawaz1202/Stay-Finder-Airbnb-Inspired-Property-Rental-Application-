package com.example.stayfinder.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stayfinder.MainActivity
import com.example.stayfinder.R
import com.example.stayfinder.firebase.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etName = view.findViewById<TextInputEditText>(R.id.etDisplayName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etRegPassword)
        val btn = view.findViewById<MaterialButton>(R.id.btnRegister)
        val tvBack = view.findViewById<View>(R.id.tvBackLogin)

        btn.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            if (name.length < 2 || email.isEmpty() || password.length < 6) {
                Toast.makeText(requireContext(), R.string.password_min, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                view.findViewById<ProgressBar>(R.id.progressRegister).visibility = View.VISIBLE
                authRepository.registerWithEmail(email, password, name).fold(
                    onSuccess = {
                        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        requireActivity().finish()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                )
                view.findViewById<ProgressBar>(R.id.progressRegister).visibility = View.GONE
            }
        }

        tvBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
