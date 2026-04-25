package com.example.stayfinder

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stayfinder.database.DatabaseManager
import com.example.stayfinder.models.Booking
import com.google.android.material.button.MaterialButton

class DetailFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_listing_details, container, false)

        dbManager = DatabaseManager(requireContext())

        val property = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("PROPERTY_DATA", Property::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("PROPERTY_DATA") as? Property
        }

        if (property != null) {
            // Bind UI
            view.findViewById<TextView>(R.id.tvDetailTitle).text = property.title
            view.findViewById<TextView>(R.id.tvDetailLocation).text = property.location
            view.findViewById<TextView>(R.id.tvDetailPrice).text = property.price
            view.findViewById<TextView>(R.id.tvDetailRating).text = property.rating
            view.findViewById<TextView>(R.id.tvDetailGuests).text = "${property.guests} Guests"
            view.findViewById<TextView>(R.id.tvDetailBedrooms).text = "${property.bedrooms} Bedrooms"
            view.findViewById<TextView>(R.id.tvDetailBathrooms).text = "${property.bathrooms} Bathrooms"
            view.findViewById<TextView>(R.id.tvDetailDescription).text = property.description

            // Favorite Button
            val imgFavorite = view.findViewById<ImageView>(R.id.imgFavoriteBtn)
            val propertyId = property.id.toIntOrNull() ?: -1
            
            if (propertyId != -1) {
                var isFav = dbManager.isFavorite(propertyId)
                updateFavoriteIcon(imgFavorite, isFav)

                imgFavorite.setOnClickListener {
                    if (isFav) {
                        dbManager.removeFavorite(propertyId)
                        isFav = false
                        Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        dbManager.addFavorite(property)
                        isFav = true
                        Toast.makeText(context, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show()
                    }
                    updateFavoriteIcon(imgFavorite, isFav)
                }
            }

            // Back Button
            view.findViewById<ImageView>(R.id.imgBackBtn).setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            // F3: Book Now Button (Add to Bookings)
            val btnBook = view.findViewById<MaterialButton>(R.id.btnBookNow)
            btnBook.text = "Book Now — ${property.price}"
            btnBook.setOnClickListener {
                val newBooking = Booking(
                    propertyId = propertyId,
                    propertyName = property.title,
                    checkInDate = "2025-05-10", // Sample date
                    checkOutDate = "2025-05-15", // Sample date
                    guestName = "User Name", // Sample name
                    totalPrice = property.price // Simplified
                )
                
                val result = dbManager.addBooking(newBooking)
                if (result != -1L) {
                    Toast.makeText(context, "Booking saved to local database! 📅", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save booking", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun updateFavoriteIcon(imgView: ImageView, isFav: Boolean) {
        imgView.setImageResource(
            if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }
}
