package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

data class ReceivedDataModel(
    // Matches the "message" key from the server's JSON
    val message: String,

    // Matches the "messages_received" key from the server
    // @SerializedName is used here for clarity, but is optional
    @SerializedName("messages_received")
    val messagesReceived: List<String>
)

