package com.example.stayfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(
    private val propertyList: List<Property>,
    private val onItemClick: (Property) -> Unit
) : RecyclerView.Adapter<HomeAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvPriceBadge: TextView = itemView.findViewById(R.id.tvPriceBadge)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvGuests: TextView = itemView.findViewById(R.id.tvGuests)
        val tvBedrooms: TextView = itemView.findViewById(R.id.tvBedrooms)
        val tvBathrooms: TextView = itemView.findViewById(R.id.tvBathrooms)
        val imgHouse: ImageView = itemView.findViewById(R.id.imgHouse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_house_listing, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList[position]
        holder.tvTitle.text = property.title
        holder.tvLocation.text = property.location
        holder.tvPriceBadge.text = property.price
        holder.tvRating.text = property.rating
        holder.tvGuests.text = "${property.guests} Guests"
        holder.tvBedrooms.text = "${property.bedrooms} Beds"
        holder.tvBathrooms.text = "${property.bathrooms} Baths"
        
        // Use Glide for image loading
        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(property.imageUrl)
            .placeholder(R.drawable.ic_home)
            .error(R.drawable.ic_home)
            .into(holder.imgHouse)

        holder.itemView.setOnClickListener {
            onItemClick(property)
        }
    }

    override fun getItemCount(): Int = propertyList.size
}
