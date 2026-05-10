package com.stayfinder.app.repository

import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.models.Property
import com.stayfinder.app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class PropertyRepository(private val db: DatabaseHelper) {

    suspend fun fetchAndMapProperties(userId: Long): List<Property> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.getPosts()
            val locations = listOf("Lahore", "Karachi", "Islamabad", "Dubai", "Istanbul", "London", "New York", "Bali")
            
            response.map { post ->
                val name = post.title.take(30).split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
                
                val location = locations[post.id % locations.size]
                val price = (post.id * 17 + 50).toDouble()
                val rating = ((post.id % 5) + 1) * 0.9
                val type = when (post.id % 3) {
                    0 -> "Entire Stay"
                    1 -> "Private Room"
                    else -> "Shared"
                }
                
                Property(
                    apiPostId = post.id,
                    name = name,
                    location = location,
                    pricePerNight = price,
                    rating = rating,
                    type = type,
                    isInWishlist = db.isPropertyInWishlist(userId, post.id)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
