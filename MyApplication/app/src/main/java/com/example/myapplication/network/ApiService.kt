package com.example.myapplication.network

import com.example.myapplication.bingocards.BingoCard
import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
        @POST("join-game")
        suspend fun joinGame(@Body playerRegistration: PlayerRegistration): Response<RegistrationResponse>

        @POST("create-game")
        suspend fun createGame(@Body playerRegistration: PlayerRegistration): Response<RegistrationResponse>

        @GET("players/{gameId}")
        suspend fun getPlayers(@Path("gameId") gameId: String): List<Player>

        @DELETE("players/{gameId}/{playerId}")
        suspend fun removePlayer(
                @Path("playerId") playerId: String,
                @Path("gameId") gameId: String
        ): Response<RegistrationResponse>

        @GET("player-card/{gameId}/{playerId}")
        suspend fun getBingoCard(
                @Path("gameId") gameId: String,
                @Path("playerId") playerId: String
        ): BingoCard

        @POST("player-card/{cardId}/{number}")
        suspend fun clickNumberOnBingoCard(
                @Path("number") number: Int,
                @Path("cardId") cardId: String?
        ): Boolean
}
