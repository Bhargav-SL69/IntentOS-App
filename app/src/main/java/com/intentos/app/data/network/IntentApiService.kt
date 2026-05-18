package com.intentos.app.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IntentApiService {
    
    @GET("/health")
    suspend fun checkHealth(): Map<String, String>

    @POST("/api/v1/intent/infer")
    suspend fun inferIntent(@Body request: InferenceRequest): IntentResponse
}
