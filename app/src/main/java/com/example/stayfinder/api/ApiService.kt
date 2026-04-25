package com.example.stayfinder.api

import com.example.stayfinder.models.PropertyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("photos")
    suspend fun getProperties(
        @Query("_limit") limit: Int = 20
    ): Response<List<PropertyResponse>>
}
