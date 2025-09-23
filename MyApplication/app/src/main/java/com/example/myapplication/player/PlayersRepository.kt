
import android.content.Context
import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ApiService
import com.example.myapplication.player.Player
import com.example.myapplication.player.PlayerRegistration
import com.example.myapplication.player.RegistrationResponse
import retrofit2.Response
import java.io.IOException
import androidx.core.content.edit

class PlayersRepository(private val apiService: ApiService,
                        private val applicationContext: Context
) {
    private val sharedPrefs = applicationContext.getSharedPreferences("bingo_prefs", Context.MODE_PRIVATE)

    fun savePlayerInfo(gameId: String, playerId: String) {
        sharedPrefs.edit {
            putString("gameId", gameId)
            putString("playerId", playerId)
            apply()
        }
    }

    fun getPlayerInfo(): Pair<String?, String?> {
        val gameId = sharedPrefs.getString("gameId", null)
        val playerId = sharedPrefs.getString("playerId", null)
        return Pair(gameId, playerId)
    }

    fun clearPlayerInfo() {
        sharedPrefs.edit { clear() }
    }
    suspend fun createGame(playerName: String): ApiResult<RegistrationResponse> {
        return try {
            val response: Response<RegistrationResponse> = apiService.createGame(
                playerRegistration = PlayerRegistration(name = playerName)
            )
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

    suspend fun joinGame(playerName: String, gameId: String): ApiResult<RegistrationResponse> {
        return try {
            val response: Response<RegistrationResponse> = apiService.joinGame(
                playerRegistration = PlayerRegistration(name = playerName, gameId = gameId)
            )
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
            ApiResult.Error("An unexpected error occurred during game join: ${e.message}")
        }
    }

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

    /*suspend fun removePlayer(player: Player): ApiResult<Unit> {
        return try {
            val response: Response<Unit> = apiService.removePlayer(player.playerId, player.gameId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("An unexpected error occurred from removePlayer: ${e.message}")
        }
    }*/
}