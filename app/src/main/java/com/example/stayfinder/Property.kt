package com.example.stayfinder

import java.io.Serializable

data class Property(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val description: String = "",
    val price: String = "",
    val priceValue: Double = 0.0,
    val rating: String = "0.0",
    val guests: Int = 1,
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val propertyType: String = "House",
    val imageUrl: String = "",
    val createdAtMillis: Long = 0L,
    val tags: ArrayList<String> = arrayListOf(),
    val ownerUid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Serializable
