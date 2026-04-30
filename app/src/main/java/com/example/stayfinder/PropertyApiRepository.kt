package com.example.stayfinder

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PropertyApiRepository(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            // using a dummy mock base URL as instructed ("e.g. RapidAPI Real Estate")
//            .baseUrl("https://mockapi.example.com/")
            .baseUrl("https://69f2f332b15130b973535eef.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Requirement F1: Fetches property listings from REST API.
     * Requirement F4: syncToLocal() - inserts new listings into local SQLite for offline caching.
     */
    suspend fun fetchListings() {
        withContext(Dispatchers.IO) {
            try {
                val apiListings = apiService.getListings()
                syncToLocal(apiListings)
            } catch (e: Exception) {
                // Network error or Mock URL resolution failure.
                // Fallback to seeding dummy data so the UI continues to function.
                if (dbHelper.getAllListings().size < 8) {
                    seedFallbackListings()
                }
            }
        }
    }

    private fun syncToLocal(apiListings: List<ApiListing>) {
        val existingTitles = dbHelper.getAllListings().map { it.title }.toSet()

        for (apiItem in apiListings) {
            if (!existingTitles.contains(apiItem.title)) {
                val newEntity = ListingEntity(
                    title = apiItem.title,
                    price = apiItem.price,
                    amenities = apiItem.amenities,
                    imageUrl = apiItem.imageUrl
                )
                dbHelper.insertListing(newEntity)
            }
        }
    }

    private fun seedFallbackListings() {
        val samples = listOf(
            ListingEntity(title = "Luxury Beach House", price = "120", amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Parking", imageUrl = "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Mountain Cabin Retreat", price = "90", amenities = "Free WiFi, Kitchen, Parking, Fire Pit", imageUrl = "https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Downtown Loft", price = "150", amenities = "Free WiFi, Air Conditioning, Gym Access", imageUrl = "https://images.unsplash.com/photo-1568605114967-8130f3a36994?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Countryside Villa", price = "200", amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Garden, BBQ", imageUrl = "https://images.unsplash.com/photo-1449156001437-3a16d1daae39?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Modern White House", price = "180", amenities = "Smart Home, Garage, Garden, WiFi", imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Maple Tree Cottage", price = "110", amenities = "Nature View, Fireplace, WiFi, Kitchen", imageUrl = "https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Seaside Bungalow", price = "130", amenities = "Ocean View, WiFi, Balcony", imageUrl = "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80"),
            ListingEntity(title = "Urban Penthouse", price = "300", amenities = "Skyline View, Pool, Gym, WiFi", imageUrl = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80")
        )
        samples.forEach { dbHelper.insertListing(it) }
    }
}
