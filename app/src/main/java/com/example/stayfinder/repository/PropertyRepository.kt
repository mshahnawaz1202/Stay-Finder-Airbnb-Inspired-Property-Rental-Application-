package com.example.stayfinder.repository

import com.example.stayfinder.Property
import com.example.stayfinder.api.RetrofitInstance
import com.example.stayfinder.models.PropertyResponse

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
        val priceVal = (100..500).random().toDouble()
        return Property(
            id = apiModel.id.toString(),
            title = apiModel.title,
            location = "Sample Location",
            description = "This is a beautiful property fetched from API.",
            price = "$${priceVal.toInt()}/night",
            priceValue = priceVal,
            rating = String.format("%.1f", (3.5 + (1.5 * Math.random()))),
            imageUrl = apiModel.thumbnailUrl,
            tags = arrayListOf("city"),
            latitude = 0.0,
            longitude = 0.0
        )
    }
}
