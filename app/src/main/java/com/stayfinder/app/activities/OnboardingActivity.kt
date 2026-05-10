package com.stayfinder.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.stayfinder.app.R
import com.stayfinder.app.adapters.OnboardingPagerAdapter
import com.stayfinder.app.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                if (position == 2) {
                    binding.btnNext.text = getString(R.string.onboarding_get_started)
                } else {
                    binding.btnNext.text = getString(R.string.onboarding_next)
                }
            }
        })

        binding.btnSkip.setOnClickListener {
            navigateToLogin()
        }

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < 2) {
                binding.viewPager.currentItem += 1
            } else {
                navigateToLogin()
            }
        }
    }

    private fun updateDots(position: Int) {
        binding.dot1.setImageResource(if (position == 0) R.drawable.ic_dot_active else R.drawable.ic_dot_inactive)
        binding.dot2.setImageResource(if (position == 1) R.drawable.ic_dot_active else R.drawable.ic_dot_inactive)
        binding.dot3.setImageResource(if (position == 2) R.drawable.ic_dot_active else R.drawable.ic_dot_inactive)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}