package com.stayfinder.app.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: Long = 0,
    var fullName: String = "",
    var email: String = "",
    var password: String = "",
    var role: String = "Guest",
    var profileBio: String = "",
    var phoneNumber: String = "",
    var dateJoined: String = "",
    var avatarColor: String = "#FF385C",
    var preferences: String = ""
)
