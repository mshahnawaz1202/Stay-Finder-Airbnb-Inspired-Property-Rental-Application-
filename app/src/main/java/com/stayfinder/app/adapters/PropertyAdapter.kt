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

class PropertyAdapter(
    private val onCardClick: (Property) -> Unit,
    private val onHeartClick: (Property, Int) -> Unit
) : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val itemBinding = ItemPropertyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val property = getItem(position)
            holder.updateHeart(property.isInWishlist)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class PropertyViewHolder(private val itemBinding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(property: Property) {
            itemBinding.tvPropertyName.text = property.name
            itemBinding.tvLocation.text = property.location
            itemBinding.tvPrice.text = String.format(Locale.getDefault(), "PKR %,.0f / night", property.pricePerNight)
            itemBinding.tvRating.text = String.format(Locale.getDefault(), "%.1f", property.rating)
            itemBinding.tvType.text = property.type
            
            updateHeart(property.isInWishlist)

            itemBinding.root.setOnClickListener { onCardClick(property) }
            itemBinding.ivFavorite.setOnClickListener { onHeartClick(property, bindingAdapterPosition) }
        }

        fun updateHeart(isInWishlist: Boolean) {
            itemBinding.ivFavorite.setImageResource(
                if (isInWishlist) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }
    }

    class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.apiPostId == newItem.apiPostId
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Property, newItem: Property): Any? {
            return if (oldItem.isInWishlist != newItem.isInWishlist) true else null
        }
    }
}
