package com.example.stayfinder

import retrofit2.http.GET

data class ApiListing(
    val title: String,
    val price: String,
    val amenities: String,
    val imageUrl: String
)

interface ApiService {
    @GET("listings")
    suspend fun getListings(): List<ApiListing>
}
