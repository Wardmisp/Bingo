package com.example.myapplication.player

import com.google.gson.annotations.SerializedName

data class Player(
    val name: String?
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
    @SerializedName("id")
    val playerId: String,
    val gameId: String
)
