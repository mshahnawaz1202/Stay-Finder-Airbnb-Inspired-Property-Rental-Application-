package com.stayfinder.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.stayfinder.app.adapters.BookingAdapter
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.FragmentTripsBinding
import com.stayfinder.app.models.Booking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: BookingAdapter
    private var allBookings = listOf<Booking>()
    private var userId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getLong("USER_ID") ?: -1L
        db = DatabaseHelper.getInstance(requireContext())

        setupRecyclerView()
        setupFilters()
        loadBookings()
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter()
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }
        binding.rvTrips.layoutManager = layoutManager
        binding.rvTrips.adapter = adapter
    }

    private fun loadBookings() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bookings = db.getBookingsByUser(userId)
            allBookings = bookings
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                filterBookings("Confirmed")
                binding.tvEmptyTrips.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupFilters() {
        binding.rgTripFilter.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                com.stayfinder.app.R.id.rb_upcoming -> filterBookings("Confirmed")
                com.stayfinder.app.R.id.rb_completed -> filterBookings("Completed")
                com.stayfinder.app.R.id.rb_cancelled -> filterBookings("Cancelled")
            }
        }
    }

    private fun filterBookings(status: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val filtered = when (status) {
            "Confirmed" -> allBookings.filter { it.status == "Confirmed" && it.checkIn >= today }
            "Completed" -> allBookings.filter { it.checkOut < today }
            "Cancelled" -> allBookings.filter { it.status == "Cancelled" }
            else -> allBookings
        }
        adapter.submitList(filtered)
        binding.tvEmptyTrips.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
