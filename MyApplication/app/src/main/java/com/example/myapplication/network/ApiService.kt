package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/api/data")
    suspend fun getData(): Response<ReceivedDataModel>
}