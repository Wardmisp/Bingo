package com.example.myapplication.network

import com.example.myapplication.player.Player
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
        @POST("register-player")
        suspend fun registerPlayer(@Body playerInfo: Map<String, String>): Response<RegistrationResponse>

        @GET("players/{gameId}")
        suspend fun getPlayers(@Path("gameId") gameId: String): List<Player>
}