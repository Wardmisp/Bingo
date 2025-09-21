package com.example.myapplication.bingocards

import com.google.gson.annotations.SerializedName

data class BingoCard(
    val card: List<List<Int>>,
    val cardId: String
)