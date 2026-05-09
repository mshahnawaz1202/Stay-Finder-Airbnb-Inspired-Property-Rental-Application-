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
import androidx.lifecycle.lifecycleScope
import com.example.stayfinder.database.DatabaseManager
import com.example.stayfinder.firebase.FirestoreFavoritesRepository
import com.example.stayfinder.models.Booking
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {

    private lateinit var dbManager: DatabaseManager
    private val favRepo = FirestoreFavoritesRepository()

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
            view.findViewById<TextView>(R.id.tvDetailTitle).text = property.title
            view.findViewById<TextView>(R.id.tvDetailLocation).text = property.location
            view.findViewById<TextView>(R.id.tvDetailPrice).text = property.price
            view.findViewById<TextView>(R.id.tvDetailRating).text = property.rating
            view.findViewById<TextView>(R.id.tvDetailGuests).text = "${property.guests} Guests"
            view.findViewById<TextView>(R.id.tvDetailBedrooms).text = "${property.bedrooms} Bedrooms"
            view.findViewById<TextView>(R.id.tvDetailBathrooms).text = "${property.bathrooms} Bathrooms"
            view.findViewById<TextView>(R.id.tvDetailDescription).text = property.description

            val imgFavorite = view.findViewById<ImageView>(R.id.imgFavoriteBtn)
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            fun refreshFavoriteUi(isFav: Boolean) {
                updateFavoriteIcon(imgFavorite, isFav)
            }

            lifecycleScope.launch {
                val isFav = if (uid != null) {
                    favRepo.isFavorite(uid, property.id)
                } else {
                    dbManager.isFavorite(property.id)
                }
                refreshFavoriteUi(isFav)
            }

            imgFavorite.setOnClickListener {
                lifecycleScope.launch {
                    if (uid != null) {
                        val currently = favRepo.isFavorite(uid, property.id)
                        if (currently) {
                            favRepo.removeFavorite(uid, property.id)
                            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                            refreshFavoriteUi(false)
                        } else {
                            favRepo.setFavorite(uid, property)
                            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                            refreshFavoriteUi(true)
                        }
                    } else {
                        var local = dbManager.isFavorite(property.id)
                        if (local) {
                            dbManager.removeFavorite(property.id)
                            local = false
                            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            dbManager.addFavorite(property)
                            local = true
                            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                        }
                        refreshFavoriteUi(local)
                    }
                }
            }

            view.findViewById<ImageView>(R.id.imgBackBtn).setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            val btnBook = view.findViewById<MaterialButton>(R.id.btnBookNow)
            btnBook.text = "Book Now — ${property.price}"
            btnBook.setOnClickListener {
                val guest = FirebaseAuth.getInstance().currentUser?.displayName ?: "Guest"
                val newBooking = Booking(
                    propertyId = property.id,
                    propertyName = property.title,
                    checkInDate = "2026-05-10",
                    checkOutDate = "2026-05-15",
                    guestName = guest,
                    totalPrice = property.price
                )

                val result = dbManager.addBooking(newBooking)
                if (result != -1L) {
                    Toast.makeText(context, "Booking saved", Toast.LENGTH_LONG).show()
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
