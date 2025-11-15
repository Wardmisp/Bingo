package com.example.myapplication.gamestatus

import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import java.io.IOException

class GameRepository(private val apiService: ApiService) {
    suspend fun launchGame(gameId: String): ApiResult<Unit>{
        return try {
            val response = apiService.launchGame(gameId.toInt())
            if (response.isSuccessful) {
                val launchGameResponse = response.body()
                if (launchGameResponse != null) {
                    ApiResult.Success(launchGameResponse)
                } else {
                    ApiResult.Error("Response body is empty or missing launch game data.")
                }
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred during game launch: ${e.message}")
        }
    }

}