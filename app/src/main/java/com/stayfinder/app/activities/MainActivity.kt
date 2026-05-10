package com.stayfinder.app.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.stayfinder.app.R
import com.stayfinder.app.databinding.ActivityMainBinding
import com.stayfinder.app.fragments.HomeFragment
import com.stayfinder.app.fragments.ProfileFragment
import com.stayfinder.app.fragments.TripsFragment
import com.stayfinder.app.fragments.WishlistFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentUserId: Long = -1L
    private var currentUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        currentUserName = intent.getStringExtra("USER_NAME")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount <= 0) {
                    showExitDialog()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), false)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> loadFragment(HomeFragment(), true)
                R.id.nav_wishlists -> loadFragment(WishlistFragment(), true)
                R.id.nav_trips -> loadFragment(TripsFragment(), true)
                R.id.nav_profile -> loadFragment(ProfileFragment(), true)
                else -> false
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean): Boolean {
        val bundle = Bundle()
        bundle.putLong("USER_ID", currentUserId)
        bundle.putString("USER_NAME", currentUserName)
        fragment.arguments = bundle
        
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit Stay Finder")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}
