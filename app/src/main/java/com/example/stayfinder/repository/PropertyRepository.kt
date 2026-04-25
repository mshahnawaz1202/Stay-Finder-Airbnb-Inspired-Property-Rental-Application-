package com.example.stayfinder.repository

import com.example.stayfinder.Property
import com.example.stayfinder.api.RetrofitInstance
import com.example.stayfinder.models.PropertyResponse
import retrofit2.Response

class PropertyRepository {
    suspend fun getProperties(): List<Property> {
        val response = RetrofitInstance.api.getProperties()
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.map { apiModel ->
                mapToProperty(apiModel)
            }
        } else {
            emptyList()
        }
    }

    private fun mapToProperty(apiModel: PropertyResponse): Property {
        return Property(
            id = apiModel.id.toString(),
            title = apiModel.title,
            location = "Sample Location", // API doesn't provide this
            description = "This is a beautiful property fetched from API.",
            price = "$${(100..500).random()}/night",
            priceValue = (100..500).random().toDouble(),
            rating = String.format("%.1f", (3.5 + (1.5 * Math.random()))),
            imageUrl = apiModel.thumbnailUrl
        )
    }
}
