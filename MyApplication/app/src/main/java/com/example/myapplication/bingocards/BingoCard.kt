package com.example.myapplication.bingocards

import com.google.gson.annotations.SerializedName

data class BingoCard(
    @SerializedName("card")
    val card: List<List<Int>>
)