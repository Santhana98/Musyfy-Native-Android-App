package com.musyfy.nativeapp.data.remote.api

import retrofit2.http.GET

interface ApiService {
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse
}

data class HealthResponse(
    val status: String,
    val version: String
)
