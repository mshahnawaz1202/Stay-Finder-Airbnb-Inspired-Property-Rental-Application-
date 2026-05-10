package com.stayfinder.app.models

data class Property(
    val id: Int = 0,
    val apiPostId: Int = 0,
    val name: String,
    val location: String,
    val pricePerNight: Double,
    val rating: Double,
    val type: String,
    val userId: Int = 0,
    val dateSaved: String = "",
    var isInWishlist: Boolean = false
)