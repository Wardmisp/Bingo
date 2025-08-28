// src/main/java/com/example/bingoapp/repository/PlayersRepository.kt

import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import retrofit2.HttpException

class PlayersRepository(private val apiService: ApiService) {

    suspend fun fetchPlayers(): ApiResult<List<Player>> {
        return try {
            val response = apiService.getPlayers()
            if (response.isSuccessful) {
                val players = response.body()
                if (players != null) {
                    ApiResult.Success(players)
                } else {
                    ApiResult.Error("Response body is empty.")
                }
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network Error: ${e.message}")
        }
    }

    /**
     * Registers a new player with the server.
     * @param playerName The name of the player to register.
     * @return An ApiResult indicating success or failure of the registration.
     */
    suspend fun registerPlayer(playerName: String): ApiResult<RegistrationResponse> {
        return try {
            val response = apiService.registerPlayer(PlayerRegistration(playerName))
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Response body is empty.")
            } else {
                // Handle specific HTTP errors like a 409 Conflict if the player already exists.
                val errorMessage = when (response.code()) {
                    409 -> "Player '${playerName}' already exists."
                    else -> "API Error: ${response.code()} - ${response.message()}"
                }
                ApiResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            // Handle HttpException to catch non-2xx codes and get more info.
            ApiResult.Error("HTTP Error: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            ApiResult.Error("Network Error: ${e.message}")
        }
    }
}