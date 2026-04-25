package com.example.stayfinder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfinder.R
import com.example.stayfinder.models.Booking
import com.google.android.material.button.MaterialButton

class BookingAdapter(
    private var bookingList: List<Booking>,
    private val onEditClick: (Booking) -> Unit,
    private val onDeleteClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPropertyName: TextView = itemView.findViewById(R.id.tvBookingPropertyName)
        val tvDates: TextView = itemView.findViewById(R.id.tvBookingDates)
        val tvGuestName: TextView = itemView.findViewById(R.id.tvBookingGuestName)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvBookingTotalPrice)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEditBooking)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDeleteBooking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.tvPropertyName.text = booking.propertyName
        holder.tvDates.text = "${booking.checkInDate} - ${booking.checkOutDate}"
        holder.tvGuestName.text = "Guest: ${booking.guestName}"
        holder.tvTotalPrice.text = "${booking.totalPrice} Total"

        holder.btnEdit.setOnClickListener { onEditClick(booking) }
        holder.btnDelete.setOnClickListener { onDeleteClick(booking) }
    }

    override fun getItemCount(): Int = bookingList.size

    fun updateList(newList: List<Booking>) {
        bookingList = newList
        notifyDataSetChanged()
    }
}
