package com.stayfinder.app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.stayfinder.app.R
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.FragmentPropertyDetailBinding
import com.stayfinder.app.models.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PropertyDetailFragment : Fragment() {

    private var _binding: FragmentPropertyDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper
    private var userId: Long = -1L
    private var apiPostId: Int = -1
    private var propertyName: String = ""
    private var propertyLocation: String = ""
    private var pricePerNight: Double = 0.0
    private var rating: Double = 0.0
    private var type: String = ""
    private var isInWishlist: Boolean = false

    private var checkInDate: Calendar? = null
    private var checkOutDate: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper.getInstance(requireContext())

        arguments?.let {
            propertyName = it.getString("PROPERTY_NAME", "")
            propertyLocation = it.getString("PROPERTY_LOCATION", "")
            pricePerNight = it.getDouble("PROPERTY_PRICE", 0.0)
            rating = it.getDouble("PROPERTY_RATING", 0.0)
            type = it.getString("PROPERTY_TYPE", "")
            userId = it.getLong("USER_ID", -1L)
            apiPostId = it.getInt("API_POST_ID", -1)
            isInWishlist = it.getBoolean("IS_IN_WISHLIST", false)
        }

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_home_nav) 
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        
        binding.tvDetailName.text = propertyName
        binding.tvDetailLocation.text = propertyLocation
        
        val locale = Locale.getDefault()
        binding.tvDetailPrice.text = getString(R.string.price_per_night, String.format(locale, "%,.0f", pricePerNight))
        binding.tvDetailRating.text = getString(R.string.rating_format, rating)
        binding.tvDetailType.text = type
        
        binding.tvDetailDescription.text = getString(R.string.about_this_place_desc, propertyName, propertyLocation, type)

        updateWishlistButton()

        binding.btnSaveWishlist.setOnClickListener { toggleWishlist() }
        
        binding.btnCheckIn.setOnClickListener { showDatePicker(true) }
        binding.btnCheckOut.setOnClickListener { showDatePicker(false) }
        
        binding.btnConfirmBooking.setOnClickListener { confirmBooking() }
    }

    private fun updateWishlistButton() {
        binding.btnSaveWishlist.setText(if (isInWishlist) R.string.remove_from_wishlist else R.string.save_to_wishlist)
        binding.btnSaveWishlist.setIconResource(if (isInWishlist) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }

    private fun toggleWishlist() {
        if (userId == -1L) {
            Snackbar.make(binding.root, "Please login to save to wishlist", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (isInWishlist) {
                db.removeFromWishlist(userId, apiPostId)
            } else {
                val property = Property(
                    apiPostId = apiPostId,
                    name = propertyName,
                    location = propertyLocation,
                    pricePerNight = pricePerNight,
                    rating = rating,
                    type = type,
                    dateSaved = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                db.addToWishlist(userId, property)
            }
            isInWishlist = !isInWishlist
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                updateWishlistButton()
                val msg = if (isInWishlist) R.string.added_to_wishlist else R.string.removed_from_wishlist
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isCheckIn: Boolean) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val selected = Calendar.getInstance().apply { set(year, month, day) }
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            if (isCheckIn) {
                checkInDate = selected
                binding.btnCheckIn.text = sdf.format(selected.time)
            } else {
                checkOutDate = selected
                binding.btnCheckOut.text = sdf.format(selected.time)
            }
            calculateTotal()
        }

        val dialog = DatePickerDialog(
            requireContext(), 
            dateSetListener, 
            calendar.get(Calendar.YEAR), 
            calendar.get(Calendar.MONTH), 
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        if (!isCheckIn && checkInDate != null) {
            dialog.datePicker.minDate = checkInDate!!.timeInMillis + 86400000
        } else {
            dialog.datePicker.minDate = System.currentTimeMillis()
        }
        dialog.show()
    }

    private fun calculateTotal() {
        val start = checkInDate
        val end = checkOutDate
        if (start != null && end != null) {
            val diff = end.timeInMillis - start.timeInMillis
            val nights = (diff / (1000 * 60 * 60 * 24)).toInt()
            if (nights > 0) {
                val total = nights * pricePerNight
                binding.tvTotalPrice.text = getString(R.string.total_price_for_nights, String.format(Locale.getDefault(), "%,.0f", total), nights)
            } else {
                binding.tvTotalPrice.text = ""
            }
        }
    }

    private fun confirmBooking() {
        if (userId == -1L) {
            Snackbar.make(binding.root, "Please login to book", Snackbar.LENGTH_SHORT).show()
            return
        }

        val start = checkInDate
        val end = checkOutDate
        if (start == null || end == null) {
            Snackbar.make(binding.root, "Please select dates", Snackbar.LENGTH_SHORT).show()
            return
        }

        val diff = end.timeInMillis - start.timeInMillis
        val nights = (diff / (1000 * 60 * 60 * 24)).toInt()
        val total = nights * pricePerNight
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val property = Property(name = propertyName, location = propertyLocation, pricePerNight = pricePerNight, rating = rating, type = type)
            val bookingId = db.insertBooking(userId, property, sdf.format(start.time), sdf.format(end.time), total)
            
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                if (bookingId != -1L) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.booking_confirmed)
                        .setMessage("Summary:\n$propertyName\n$nights nights\nTotal: PKR ${String.format(Locale.getDefault(), "%,.0f", total)}")
                        .setPositiveButton("OK") { _, _ -> parentFragmentManager.popBackStack() }
                        .show()
                } else {
                    Snackbar.make(binding.root, "Booking failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
