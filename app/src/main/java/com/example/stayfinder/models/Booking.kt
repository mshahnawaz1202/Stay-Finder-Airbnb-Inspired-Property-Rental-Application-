package com.example.stayfinder.models

data class Booking(
    var id: String = "",
    val propertyId: Int = 0,
    val propertyName: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val guestName: String = "",
    val totalPrice: String = ""
)
