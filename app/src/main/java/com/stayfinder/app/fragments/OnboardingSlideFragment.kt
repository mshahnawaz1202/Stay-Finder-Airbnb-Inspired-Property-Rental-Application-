package com.stayfinder.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stayfinder.app.R
import com.stayfinder.app.databinding.FragmentOnboardingSlideBinding

class OnboardingSlideFragment : Fragment() {

    private var _binding: FragmentOnboardingSlideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingSlideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = arguments?.getInt(ARG_POSITION) ?: 0

        when (position) {
            0 -> {
                binding.ivOnboarding.setImageResource(R.drawable.ic_house_vector)
                binding.tvTitle.text = getString(R.string.onboarding_title_1)
                binding.tvDescription.text = getString(R.string.onboarding_desc_1)
            }
            1 -> {
                binding.ivOnboarding.setImageResource(R.drawable.ic_calendar_vector)
                binding.tvTitle.text = getString(R.string.onboarding_title_2)
                binding.tvDescription.text = getString(R.string.onboarding_desc_2)
            }
            2 -> {
                binding.ivOnboarding.setImageResource(R.drawable.ic_earn_vector)
                binding.tvTitle.text = getString(R.string.onboarding_title_3)
                binding.tvDescription.text = getString(R.string.onboarding_desc_3)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingSlideFragment {
            val fragment = OnboardingSlideFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}
