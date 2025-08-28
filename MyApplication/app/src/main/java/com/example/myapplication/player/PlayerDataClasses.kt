package com.example.myapplication.player

data class Player(
    val name: String
)

data class PlayerRegistration(
    val playerName: String
)

data class RegistrationResponse(
    val status: String,
    val message: String,
    val playerId: Int
)