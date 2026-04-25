package com.example.stayfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val items: MutableList<Pair<FavoriteEntity, ListingEntity>>,
    private val onRemove: (Long) -> Unit,
    private val onEditNote: (FavoriteEntity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFavTitle: TextView      = itemView.findViewById(R.id.tvFavTitle)
        val tvFavPrice: TextView      = itemView.findViewById(R.id.tvFavPrice)
        val tvFavAmenities: TextView  = itemView.findViewById(R.id.tvFavAmenities)
        val tvFavNote: TextView       = itemView.findViewById(R.id.tvFavNote)
        val btnRemoveFav: ImageButton = itemView.findViewById(R.id.btnRemoveFav)
        val btnEditNote: TextView     = itemView.findViewById(R.id.btnEditNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val (fav, listing) = items[position]

        holder.tvFavTitle.text     = listing.title
        holder.tvFavPrice.text     = listing.price
        holder.tvFavAmenities.text = if (listing.amenities.isNotEmpty())
            listing.amenities else "No amenities listed"
        holder.tvFavNote.text      = if (fav.note.isNotEmpty())
            "📝 ${fav.note}" else "No personal note"

        holder.btnRemoveFav.setOnClickListener {
            onRemove(fav.id)
        }

        holder.btnEditNote.setOnClickListener {
            onEditNote(fav)
        }
    }

    override fun getItemCount(): Int = items.size
}
