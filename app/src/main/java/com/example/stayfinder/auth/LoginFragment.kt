package com.example.stayfinder.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stayfinder.MainActivity
import com.example.stayfinder.R
import com.example.stayfinder.firebase.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val authRepository = AuthRepository()

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java) ?: return@registerForActivityResult
            lifecycleScope.launch {
                setLoading(true)
                authRepository.signInWithGoogle(account).fold(
                    onSuccess = { openMain() },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                )
                setLoading(false)
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnEmail = view.findViewById<MaterialButton>(R.id.btnEmailLogin)
        val btnGoogle = view.findViewById<MaterialButton>(R.id.btnGoogleLogin)
        val tvRegister = view.findViewById<View>(R.id.tvGoRegister)

        btnEmail.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), R.string.sign_in_to_continue, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                setLoading(true)
                authRepository.signInWithEmail(email, password).fold(
                    onSuccess = { openMain() },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                )
                setLoading(false)
            }
        }

        btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(requireContext(), gso)
            googleLauncher.launch(client.signInIntent)
        }

        tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setLoading(loading: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressLogin)?.visibility =
            if (loading) View.VISIBLE else View.GONE
    }

    private fun openMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }
}
