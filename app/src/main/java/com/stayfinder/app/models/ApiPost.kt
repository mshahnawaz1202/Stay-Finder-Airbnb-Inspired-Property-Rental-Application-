package com.stayfinder.app.models

data class ApiPost(
    val id: Int,
    val title: String,
    val body: String,
    val userId: Int
)