package com.example.myapplication.data

import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import java.io.IOException

class PlayersRepository(private val apiService: ApiService) {

    /**
     * Creates a new game and registers the host player.
     * @param playerName The name of the host.
     * @return The response containing the new game ID.
     */
    suspend fun createGame(playerName: String): ApiResult<RegistrationResponse> {
        return try {
            val response = apiService.createGame(PlayerRegistration(playerName))
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
            ApiResult.Error("An unexpected error occurred during game creation: ${e.message}")
        }
    }

    /**
     * Adds a new player to an existing game.
     * @param playerName The name of the player to join.
     * @param gameId The ID of the game to join.
     */
    suspend fun joinGame(playerName: String, gameId: String): ApiResult<Boolean> {
        return try {
            val response: Response<RegistrationResponse> = apiService.joinGame(playerRegistration = PlayerRegistration(playerName, gameId))
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred while joining the game: ${e.message}")
        }
    }

    /**
     * Fetches the list of all players for a given game ID.
     * @param gameId The ID of the game.
     * @return A list of players.
     */
    suspend fun fetchPlayers(gameId: String): ApiResult<List<Player>> {
        return try {
            val players = apiService.getPlayers(gameId)
            ApiResult.Success(players)
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred from fetchPlayers: ${e.message}")
        }
    }
}
