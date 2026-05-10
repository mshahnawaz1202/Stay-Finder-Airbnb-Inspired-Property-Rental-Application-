package com.stayfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stayfinder.app.R
import com.stayfinder.app.databinding.ItemPropertyCardBinding
import com.stayfinder.app.models.Property
import java.util.Locale

class WishlistAdapter(
    private val onItemClick: (Property) -> Unit,
    private val onItemLongClick: (Property) -> Unit
) : ListAdapter<Property, WishlistAdapter.WishlistViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ItemPropertyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WishlistViewHolder(private val binding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            val context = itemView.context
            binding.tvPropertyName.text = property.name
            binding.tvLocation.text = property.location
            binding.tvPrice.text = context.getString(R.string.price_per_night, String.format(Locale.getDefault(), "%,.0f", property.pricePerNight))
            binding.tvRating.text = String.format(Locale.getDefault(), "%.1f", property.rating)
            binding.tvType.text = property.type
            binding.ivFavorite.setImageResource(R.drawable.ic_heart_filled)

            binding.root.setOnClickListener { onItemClick(property) }
            binding.root.setOnLongClickListener {
                onItemLongClick(property)
                true
            }
        }
    }

    class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean = oldItem == newItem
    }
}
