// src/main/java/com/example/bingoapp/repository/PlayersRepository.kt
import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import retrofit2.HttpException

class PlayersRepository(private val apiService: ApiService) {

    // A private mutable StateFlow to hold the list of players.
    // This acts as the single source of truth for player data.
    private val _players = MutableStateFlow<List<Player>>(emptyList())

    // A public read-only StateFlow that the ViewModel can collect from.
    // This prevents the UI or ViewModel from directly modifying the list.
    val players: StateFlow<List<Player>> = _players

    /**
     * Fetches all players from the server and updates the local state.
     * @return An ApiResult indicating success or failure.
     */
    suspend fun fetchPlayers(): ApiResult<Unit> {
        return try {
            val response = apiService.getPlayers()
            if (response.isSuccessful) {
                // Update the internal StateFlow with the new data from the server.
                response.body()?.let {
                    _players.value = it
                }
                ApiResult.Success(Unit)
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