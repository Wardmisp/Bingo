package com.example.myapplication.player

import com.google.gson.annotations.SerializedName

data class Player(
    val playerId: String,
    val name: String,
    val gameId: String,
    val gameStarted: Boolean,
    val isHost: Boolean
)



data class PlayerRegistration(
    @SerializedName("name")
    val name: String,
    @SerializedName("gameId")
    val gameId: String? = null
)

data class RegistrationResponse(
    val status: String,
    val message: String,
    val playerId: String,
    val gameId: String
)
