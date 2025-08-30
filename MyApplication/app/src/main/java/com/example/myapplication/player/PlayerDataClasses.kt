package com.example.myapplication.player

import com.google.gson.annotations.SerializedName

data class Player(
    val name: String?
)

data class PlayerRegistration(
    @SerializedName("name")
    //This is a name more idiomatic to Kotlin
    val name: String
)

data class RegistrationResponse(
    val status: String,
    val message: String,
    @SerializedName("id")
    val playerId: Int,
    val gameId: String
)