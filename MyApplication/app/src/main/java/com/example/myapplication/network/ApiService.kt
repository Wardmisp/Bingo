package com.example.myapplication.network

import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/data")
    suspend fun getData(): Response<ReceivedDataModel>

    @POST("/submit")
    suspend fun submitData(@Body submission: Submission): Response<Unit>

    @GET("/players")
    suspend fun getPlayers(): Response<List<Player>>

    @POST("/players/register")
    suspend fun registerPlayer(@Body playerRegistration: PlayerRegistration): Response<RegistrationResponse>
}