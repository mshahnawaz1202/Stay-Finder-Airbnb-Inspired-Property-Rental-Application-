package com.stayfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stayfinder.app.R
import com.stayfinder.app.databinding.ItemBookingCardBinding
import com.stayfinder.app.models.Booking
import java.util.Locale

class BookingAdapter : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemBookingCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvBookingProperty.text = booking.propertyName
            binding.tvBookingDates.text = itemView.context.getString(R.string.date_range, booking.checkIn, booking.checkOut)
            binding.tvBookingTotal.text = itemView.context.getString(R.string.price_pkr, String.format(Locale.getDefault(), "%,.0f", booking.totalPrice))
            binding.tvBookingStatus.text = booking.status
            
            val statusColor = if (booking.status == "Confirmed") R.color.success else R.color.error
            binding.tvBookingStatus.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tvBookingStatus.backgroundTintList = itemView.context.getColorStateList(statusColor)
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
    }
}
