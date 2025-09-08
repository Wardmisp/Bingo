package com.example.myapplication.bingocards

import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import java.io.IOException

class BingoCardsRepository(private val apiService: ApiService) {
    suspend fun fetchBingoCard(gameId: String, playerId: String): ApiResult<BingoCard> {
        return try {
            val bingoCard = apiService.getBingoCard(gameId, playerId)
            ApiResult.Success(bingoCard)
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred from fetchBingoCard: ${e.message}")
        }
    }
}