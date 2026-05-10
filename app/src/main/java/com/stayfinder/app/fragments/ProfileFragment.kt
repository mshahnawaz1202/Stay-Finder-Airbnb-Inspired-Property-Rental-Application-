package com.stayfinder.app.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.stayfinder.app.R
import com.stayfinder.app.activities.LoginActivity
import com.stayfinder.app.adapters.SearchHistoryAdapter
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper
    private var userId: Long = -1L
    private lateinit var searchAdapter: SearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getLong("USER_ID") ?: -1L
        db = DatabaseHelper.getInstance(requireContext())

        setupSearchHistory()
        loadProfile()

        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        binding.btnDeleteAccount.setOnClickListener { showDeleteAccountDialog() }
    }

    private fun setupSearchHistory() {
        searchAdapter = SearchHistoryAdapter { query ->
            Toast.makeText(requireContext(), "Search for: $query coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rvRecentSearches.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentSearches.adapter = searchAdapter
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val user = db.getUserById(userId)
            val wishlistCount = db.getWishlistCount(userId)
            val bookingCount = db.getBookingCount(userId)
            val searches = db.getSearchHistory(userId).takeLast(5)

            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                user?.let {
                    binding.tvHeaderName.text = it.fullName
                    binding.tvHeaderEmail.text = it.email
                    binding.tvRoleBadge.text = it.role
                    binding.tvMemberSince.text = getString(R.string.member_since, it.dateJoined)
                    binding.etProfileName.setText(it.fullName)
                    binding.etProfileBio.setText(it.profileBio)
                    binding.etProfilePhone.setText(it.phoneNumber)
                    
                    try {
                        binding.viewAvatarBg.background.setTint(Color.parseColor(it.avatarColor))
                    } catch (e: Exception) {
                        binding.viewAvatarBg.background.setTint(Color.parseColor("#FF385C"))
                    }
                    binding.tvAvatarInitial.text = it.fullName.firstOrNull()?.uppercase() ?: ""
                }
                binding.tvStatWishlists.text = wishlistCount.toString()
                binding.tvStatBookings.text = bookingCount.toString()
                binding.tvStatSearches.text = searches.size.toString()
                searchAdapter.submitList(searches)
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etProfileName.text.toString().trim()
        val bio = binding.etProfileBio.text.toString().trim()
        val phone = binding.etProfilePhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etProfileName.error = getString(R.string.error_name_empty)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val success = db.updateUserProfile(userId, name, bio, phone)
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                if (success) {
                    Snackbar.make(binding.root, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
                    loadProfile()
                }
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.logout_confirm)
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.email)
        
        val padding = (16 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.marginStart = padding
        params.marginEnd = padding
        input.layoutParams = params
        container.addView(input)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_confirm)
            .setView(container)
            .setPositiveButton("Delete") { _, _ ->
                val email = input.text.toString().trim()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val user = db.getUserById(userId)
                    if (user?.email == email) {
                        db.deleteUser(userId)
                        withContext(Dispatchers.Main) {
                            val intent = Intent(activity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            activity?.finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Email does not match", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
