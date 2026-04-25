package com.example.stayfinder.models

import com.google.gson.annotations.SerializedName

data class PropertyResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String
)
