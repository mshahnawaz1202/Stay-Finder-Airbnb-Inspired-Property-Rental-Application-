package com.stayfinder.app.models

data class Booking(
    val id: Int,
    val userId: Long,
    val propertyName: String,
    val propertyLocation: String,
    val pricePerNight: Double,
    val checkIn: String,
    val checkOut: String,
    val totalPrice: Double,
    val status: String,
    val bookingDate: String
)