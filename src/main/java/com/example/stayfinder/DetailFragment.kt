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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_listing_details, container, false)

        dbHelper = DatabaseHelper(requireContext())

        val property = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("PROPERTY_DATA", Property::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("PROPERTY_DATA") as? Property
        }

        if (property != null) {
            // ── Bind text fields ──────────────────────────────────────
            val tvTitle       = view.findViewById<TextView>(R.id.tvDetailTitle)
            val tvLocation    = view.findViewById<TextView>(R.id.tvDetailLocation)
            val tvPrice       = view.findViewById<TextView>(R.id.tvDetailPrice)
            val tvRating      = view.findViewById<TextView>(R.id.tvDetailRating)
            val tvGuests      = view.findViewById<TextView>(R.id.tvDetailGuests)
            val tvBedrooms    = view.findViewById<TextView>(R.id.tvDetailBedrooms)
            val tvBathrooms   = view.findViewById<TextView>(R.id.tvDetailBathrooms)
            val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)

            tvTitle?.text       = property.title
            tvLocation?.text    = property.location
            tvPrice?.text       = "$${property.priceValue.toInt()}"
            tvRating?.text      = property.rating
            tvGuests?.text      = "${property.guests} Guests"
            tvBedrooms?.text    = "${property.bedrooms} Bedrooms"
            tvBathrooms?.text   = "${property.bathrooms} Bathrooms"
            tvDescription?.text = property.description

            // ── Favourite button (toggle via DB) ──────────────────────
            val imgFavorite = view.findViewById<ImageView>(R.id.imgFavoriteBtn)
            val listingId   = property.id.toLongOrNull() ?: -1L

            if (imgFavorite != null && listingId >= 0) {
                // Set initial icon state based on DB
                lifecycleScope.launch(Dispatchers.IO) {
                    val isFav = dbHelper.isFavorite(listingId)
                    withContext(Dispatchers.Main) {
                        updateFavoriteIcon(imgFavorite, isFav)
                    }
                }

                imgFavorite.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (dbHelper.isFavorite(listingId)) {
                            // Already a favourite → remove it
                            dbHelper.deleteFavoriteByListingId(listingId)
                            withContext(Dispatchers.Main) {
                                updateFavoriteIcon(imgFavorite, false)
                                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Not a favourite → ensure listing row exists, then add
                            ensureListingInDb(listingId, property)
                            val fav = FavoriteEntity(listingId = listingId, note = "")
                            dbHelper.insertFavorite(fav)
                            withContext(Dispatchers.Main) {
                                updateFavoriteIcon(imgFavorite, true)
                                Toast.makeText(context, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            // ── Back arrow ────────────────────────────────────────────
            val imgBack = view.findViewById<ImageView>(R.id.imgBackBtn)
            imgBack?.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            // ── Book Now button ───────────────────────────────────────
            val btnBook = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBookNow)
            btnBook?.setOnClickListener {
                Toast.makeText(
                    context,
                    "Booking confirmed for ${property.title}! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    /**
     * If the listing was created from in-memory dummy data it may not yet
     * have an authored row in the DB (e.g. on a fresh install with the old
     * HomeFragment). We ensure it exists before creating the FK child row.
     */
    private fun ensureListingInDb(listingId: Long, property: Property) {
        if (dbHelper.getListingById(listingId) == null) {
            val entity = ListingEntity(
                title     = property.title,
                price     = property.price,
                amenities = property.description,
                imageUrl  = property.imageUrl
            )
            dbHelper.insertListing(entity)
        }
    }

    private fun updateFavoriteIcon(imgView: ImageView, isFav: Boolean) {
        imgView.setImageResource(
            if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
