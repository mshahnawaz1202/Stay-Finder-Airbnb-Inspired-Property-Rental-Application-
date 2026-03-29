package com.example.stayfinder

import android.os.Bundle
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_listing_details, container, false)
        
        val property = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("PROPERTY_DATA", Property::class.java)
        } else {
            arguments?.getSerializable("PROPERTY_DATA") as? Property
        }

        if (property != null) {
            val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
            val tvLocation = view.findViewById<TextView>(R.id.tvDetailLocation)
            val tvPrice = view.findViewById<TextView>(R.id.tvDetailPrice)
            val tvRating = view.findViewById<TextView>(R.id.tvDetailRating)
            val tvGuests = view.findViewById<TextView>(R.id.tvDetailGuests)
            val tvBedrooms = view.findViewById<TextView>(R.id.tvDetailBedrooms)
            val tvBathrooms = view.findViewById<TextView>(R.id.tvDetailBathrooms)
            val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)

            tvTitle?.text = property.title
            tvLocation?.text = property.location
            // Assuming price field contains symbols like "$120/night" but priceValue contains 120.0, we just show price
            tvPrice?.text = "$${property.priceValue.toInt()}" 
            tvRating?.text = property.rating
            tvGuests?.text = "${property.guests} Guests"
            tvBedrooms?.text = "${property.bedrooms} Bedrooms"
            tvBathrooms?.text = "${property.bathrooms} Bathrooms"
            tvDescription?.text = property.description
        }
        
        return view
    }
}
