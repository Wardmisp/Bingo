// src/main/java/com/example/bingoapp/repository/PlayersRepository.kt

import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import com.example.myapplication.player.Player
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import java.io.IOException

class PlayersRepository(private val apiService: ApiService) {

    suspend fun fetchPlayers(gameId: String): ApiResult<List<Player>> {
        return try {
            val players = apiService.getPlayers(gameId)
            ApiResult.Success(players)
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    suspend fun registerPlayer(playerName: String, gameId: String): ApiResult<Boolean> {
        return try {
            val response: Response<RegistrationResponse> = apiService.registerPlayer(mapOf("playerName" to playerName, "gameId" to gameId))
            if (response.isSuccessful) {
                // Return the player ID upon successful registration
                val playerId = response.body()?.playerId
                if (playerId != null) {
                    ApiResult.Success(true)
                } else {
                    ApiResult.Error("Response body is empty or missing player ID.")
                }
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    suspend fun registerPlayer(playerName: String): ApiResult<RegistrationResponse> {
        return try {
            val response = apiService.registerPlayer(mapOf("playerName" to playerName))
            if (response.isSuccessful) {
                val registrationResponse = response.body()
                if (registrationResponse != null) {
                    ApiResult.Success(registrationResponse)
                } else {
                    ApiResult.Error("Response body is empty or missing registration data.")
                }
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred: ${e.message}")
        }
    }

    fun removePlayer(player: Player): ApiResult<Boolean> {
        // Implementation for removing a player
        return ApiResult.Success(true)
    }
}