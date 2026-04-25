package com.example.stayfinder.models

data class Booking(
    val id: Int = 0,
    val propertyId: Int,
    val propertyName: String,
    val checkInDate: String,
    val checkOutDate: String,
    val guestName: String,
    val totalPrice: String
)
