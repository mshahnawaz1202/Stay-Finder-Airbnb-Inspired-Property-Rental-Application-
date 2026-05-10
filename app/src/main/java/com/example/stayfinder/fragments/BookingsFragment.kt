package com.example.stayfinder.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfinder.R
import com.example.stayfinder.adapters.BookingAdapter
import com.example.stayfinder.database.DatabaseManager
import com.example.stayfinder.models.Booking
import com.google.android.material.textfield.TextInputEditText

class BookingsFragment : Fragment() {

    private lateinit var firestoreManager: com.example.stayfinder.FirestoreManager
    private lateinit var authManager: com.example.stayfinder.FirebaseAuthManager
    private lateinit var adapter: BookingAdapter
    private var isAscending = true
    private var allBookings: List<Booking> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookings, container, false)

        firestoreManager = com.example.stayfinder.FirestoreManager()
        authManager = com.example.stayfinder.FirebaseAuthManager(requireContext())
        
        val rvBookings: RecyclerView = view.findViewById(R.id.rvBookings)
        val etSearch: EditText = view.findViewById(R.id.etSearchBookings)
        val btnSort: ImageButton = view.findViewById(R.id.btnSortBookings)

        rvBookings.layoutManager = LinearLayoutManager(context)
        adapter = BookingAdapter(emptyList(), 
            onEditClick = { booking -> showEditDialog(booking) },
            onDeleteClick = { booking -> showDeleteConfirm(booking) }
        )
        rvBookings.adapter = adapter
        
        setupRealtimeListener()

        // Search logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val results = allBookings.filter { it.propertyName.lowercase().contains(query) || it.guestName.lowercase().contains(query) }
                adapter.updateList(results)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Sort logic
        btnSort.setOnClickListener {
            isAscending = !isAscending
            val sortedList = if (isAscending) {
                allBookings.sortedBy { it.checkInDate }
            } else {
                allBookings.sortedByDescending { it.checkInDate }
            }
            adapter.updateList(sortedList)
            val order = if (isAscending) "Ascending" else "Descending"
            Toast.makeText(context, "Sorted by Date: $order", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupRealtimeListener() {
        val user = authManager.getCurrentUser()
        if (user == null) {
            Toast.makeText(context, "Please login to view bookings", Toast.LENGTH_SHORT).show()
            return
        }
        
        firestoreManager.getBookingsRef(user.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Error loading bookings", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                val bookings = snapshot.toObjects(Booking::class.java)
                allBookings = bookings
                adapter.updateList(bookings)
            }
        }
    }

    private fun showEditDialog(booking: Booking) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_booking, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditGuestName)
        val etCheckIn = dialogView.findViewById<TextInputEditText>(R.id.etEditCheckIn)
        val etCheckOut = dialogView.findViewById<TextInputEditText>(R.id.etEditCheckOut)

        etName.setText(booking.guestName)
        etCheckIn.setText(booking.checkInDate)
        etCheckOut.setText(booking.checkOutDate)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Booking")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedBooking = booking.copy(
                    guestName = etName.text.toString(),
                    checkInDate = etCheckIn.text.toString(),
                    checkOutDate = etCheckOut.text.toString()
                )
                val user = authManager.getCurrentUser()
                if (user != null) {
                    firestoreManager.updateBooking(user.uid, updatedBooking) { success ->
                        if (success) {
                            Toast.makeText(context, "Booking updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirm(booking: Booking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Booking")
            .setMessage("Are you sure you want to delete this booking for ${booking.propertyName}?")
            .setPositiveButton("Delete") { _, _ ->
                val user = authManager.getCurrentUser()
                if (user != null) {
                    firestoreManager.deleteBooking(user.uid, booking.id) { success ->
                        if (success) {
                            Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
