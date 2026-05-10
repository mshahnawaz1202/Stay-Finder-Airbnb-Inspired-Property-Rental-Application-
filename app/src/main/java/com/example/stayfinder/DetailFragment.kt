package com.example.stayfinder

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.stayfinder.database.DatabaseManager
import com.example.stayfinder.models.Booking
import com.google.android.material.button.MaterialButton

class DetailFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_listing_details, container, false)

        dbManager = DatabaseManager(requireContext())
        firestoreManager = FirestoreManager()
        authManager = FirebaseAuthManager(requireContext())

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

            // Favorite Button with Compose
            val propertyId = property.id.toIntOrNull() ?: -1
            val composeFavoriteBtn = view.findViewById<ComposeView>(R.id.composeFavoriteBtn)
            
            if (propertyId != -1) {
                composeFavoriteBtn.setContent {
                    FavoriteButtonComposable(propertyId, property)
                }
            }

            // Back Button
            view.findViewById<ImageView>(R.id.imgBackBtn).setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            // Share Button
            view.findViewById<ImageView>(R.id.imgShareBtn).setOnClickListener {
                val shareText = "Check out this amazing property: ${property.title} for just ${property.price}/night! \n\nBook now on StayFinder app! https://stayfinder.example.com/property/${propertyId}"
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Share Property via"))
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
                
                val user = authManager.getCurrentUser()
                if (user != null) {
                    firestoreManager.addBooking(user.uid, newBooking) { success ->
                        if (success) {
                            Toast.makeText(context, "Booking saved to Firestore! 📅", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to save booking to Firestore", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please login to book", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    @Composable
    fun FavoriteButtonComposable(propertyId: Int, property: Property) {
        var isFav by remember { mutableStateOf(dbManager.isFavorite(propertyId)) }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .clickable {
                    if (isFav) {
                        dbManager.removeFavorite(propertyId)
                        isFav = false
                        Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        dbManager.addFavorite(property)
                        isFav = true
                        Toast.makeText(context, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                contentDescription = "Favorite",
                tint = if (isFav) Color.Red else Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
