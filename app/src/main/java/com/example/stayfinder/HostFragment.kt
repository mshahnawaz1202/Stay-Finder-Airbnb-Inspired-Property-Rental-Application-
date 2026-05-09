package com.example.stayfinder

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stayfinder.firebase.FirestoreListingRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HostFragment : Fragment() {

    private val listingRepo = FirestoreListingRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host, container, false)

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(context, "Please sign in to host a listing", Toast.LENGTH_LONG).show()
        }

        val etTitle = view.findViewById<TextInputEditText>(R.id.etPropertyTitle)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etPropertyDescription)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPropertyPrice)
        val etLocation = view.findViewById<TextInputEditText>(R.id.etPropertyLocation)
        val rgPropertyType = view.findViewById<RadioGroup>(R.id.rgPropertyType)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitHost)

        var selectedType = "House"
        rgPropertyType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.rbHouse -> "House"
                R.id.rbApartment -> "Apt"
                R.id.rbVilla -> "Villa"
                else -> "House"
            }
        }

        btnSubmit.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                Toast.makeText(context, "Sign in required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val location = etLocation.text?.toString()?.trim().orEmpty()

            if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()) {
                val priceNum = price.toDoubleOrNull() ?: 0.0
                val fullTitle = "$selectedType: $title"
                val tags = inferTags(fullTitle, description, location)

                lifecycleScope.launch {
                    val (lat, lng) = geocodeOrDefault(location)
                    try {
                        listingRepo.addListing(
                            title = fullTitle,
                            description = description,
                            pricePerNight = priceNum,
                            propertyType = selectedType,
                            location = location.ifEmpty { "Unknown" },
                            imageUrl = "",
                            latitude = lat,
                            longitude = lng,
                            tags = tags
                        )
                        Toast.makeText(context, "Listed on Firestore", Toast.LENGTH_LONG).show()
                        etTitle.text?.clear()
                        etDescription.text?.clear()
                        etPrice.text?.clear()
                        etLocation.text?.clear()
                        rgPropertyType.check(R.id.rbHouse)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun inferTags(title: String, description: String, location: String): List<String> {
        val text = "$title $description $location".lowercase(Locale.getDefault())
        val out = mutableListOf<String>()
        if (text.contains("beach") || text.contains("ocean")) out.add("beach")
        if (text.contains("mountain") || text.contains("ski")) out.add("mountain")
        if (text.contains("city") || text.contains("downtown")) out.add("city")
        if (text.contains("village") || text.contains("country")) out.add("village")
        if (out.isEmpty()) out.add("stay")
        return out
    }

    private suspend fun geocodeOrDefault(address: String): Pair<Double, Double> {
        if (address.isBlank()) return 37.42 to -122.08
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    results[0].latitude to results[0].longitude
                } else {
                    37.42 to -122.08
                }
            } catch (_: Exception) {
                37.42 to -122.08
            }
        }
    }
}
