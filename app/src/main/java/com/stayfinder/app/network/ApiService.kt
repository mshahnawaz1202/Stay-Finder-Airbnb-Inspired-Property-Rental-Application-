package com.stayfinder.app.network

import com.stayfinder.app.models.ApiPost
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<ApiPost>
}