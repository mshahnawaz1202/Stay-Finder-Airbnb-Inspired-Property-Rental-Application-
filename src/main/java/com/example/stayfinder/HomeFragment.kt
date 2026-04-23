package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dbHelper = DatabaseHelper(requireContext())

        val rvListings: RecyclerView = view.findViewById(R.id.rvListings)
        rvListings.layoutManager = LinearLayoutManager(context)

        // ── Seed the listings table only on first launch ───────────────
        if (dbHelper.getAllListings().isEmpty()) {
            seedListings()
        }

        // ── Build Property list from DB rows ───────────────────────────
        val properties = dbHelper.getAllListings().map { entity ->
            Property(
                id            = entity.id.toString(),
                title         = entity.title,
                location      = "See details",
                description   = entity.amenities,
                price         = entity.price,
                priceValue    = entity.price
                    .replace(Regex("[^0-9.]"), "")
                    .toDoubleOrNull() ?: 0.0,
                rating        = "4.8",
                guests        = 4,
                bedrooms      = 2,
                bathrooms     = 2,
                propertyType  = "House",
                imageUrl      = entity.imageUrl
            )
        }

        val adapter = HomeAdapter(properties) { selectedProperty ->
            val detailFragment = DetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("PROPERTY_DATA", selectedProperty)
            detailFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvListings.adapter = adapter
        return view
    }

    // ── Pre-populate the listings table with sample data ───────────────
    private fun seedListings() {
        val samples = listOf(
            ListingEntity(
                title     = "Luxury Beach House",
                price     = "\$120/night",
                amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Parking",
                imageUrl  = ""
            ),
            ListingEntity(
                title     = "Mountain Cabin Retreat",
                price     = "\$90/night",
                amenities = "Free WiFi, Kitchen, Parking, Fire Pit",
                imageUrl  = ""
            ),
            ListingEntity(
                title     = "Downtown Loft",
                price     = "\$150/night",
                amenities = "Free WiFi, Air Conditioning, Gym Access",
                imageUrl  = ""
            ),
            ListingEntity(
                title     = "Countryside Villa",
                price     = "\$200/night",
                amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Garden, BBQ",
                imageUrl  = ""
            )
        )
        samples.forEach { dbHelper.insertListing(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
