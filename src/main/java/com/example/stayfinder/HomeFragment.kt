package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        val rvListings: RecyclerView = view.findViewById(R.id.rvListings)
        rvListings.layoutManager = LinearLayoutManager(context)

        // Dummy data explicitly made to impress
        val dummyList = listOf(
            Property(
                id = "1", title = "Luxury Beach House", location = "Malibu, California",
                description = "Experience the ultimate beachfront getaway in this stunning luxury house. With panoramic ocean views, private pool, and modern amenities.",
                price = "$120/night", priceValue = 120.0, rating = "4.8", guests = 4, bedrooms = 2, bathrooms = 2
            ),
            Property(
                id = "2", title = "Mountain Cabin Retreat", location = "Aspen, Colorado",
                description = "Cozy log cabin surrounded by pine trees. Perfect for a winter ski trip or a summer hike.",
                price = "$90/night", priceValue = 90.0, rating = "4.9", guests = 2, bedrooms = 1, bathrooms = 1
            ),
            Property(
                id = "3", title = "Downtown Loft", location = "New York, NY",
                description = "Modern loft in the heart of the city. Walking distance to all major attractions.",
                price = "$150/night", priceValue = 150.0, rating = "4.5", guests = 3, bedrooms = 1, bathrooms = 2
            )
        )

        val adapter = HomeAdapter(dummyList) { selectedProperty ->
            // Pass the entire object using Bundle as requested in "PRO TIP"
            val detailFragment = DetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("PROPERTY_DATA", selectedProperty)
            detailFragment.arguments = bundle
            
            // Navigate to DetailFragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        
        rvListings.adapter = adapter
        return view
    }
}
