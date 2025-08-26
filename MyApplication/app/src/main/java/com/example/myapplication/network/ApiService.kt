package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/data")
    suspend fun getData(): Response<ReceivedDataModel>

    @POST("/submit")
    suspend fun submitData(@Body submission: Submission): Response<Unit>
}