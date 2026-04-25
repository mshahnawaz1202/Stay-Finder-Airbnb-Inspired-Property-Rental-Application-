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
            .baseUrl("https://mockapi.example.com/") 
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
                if (dbHelper.getAllListings().isEmpty()) {
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
            ListingEntity(title = "Luxury Beach House", price = "\$120/night", amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Parking", imageUrl = ""),
            ListingEntity(title = "Mountain Cabin Retreat", price = "\$90/night", amenities = "Free WiFi, Kitchen, Parking, Fire Pit", imageUrl = ""),
            ListingEntity(title = "Downtown Loft", price = "\$150/night", amenities = "Free WiFi, Air Conditioning, Gym Access", imageUrl = ""),
            ListingEntity(title = "Countryside Villa", price = "\$200/night", amenities = "Free WiFi, Pool, Kitchen, Air Conditioning, Garden, BBQ", imageUrl = "")
        )
        samples.forEach { dbHelper.insertListing(it) }
    }
}
