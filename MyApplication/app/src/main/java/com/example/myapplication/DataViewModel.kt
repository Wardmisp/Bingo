package com.example.myapplication

import PlayersRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.ApiResult
import com.example.myapplication.player.Player
import com.example.myapplication.ui.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel(private val repository: PlayersRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId

    private var currentLobbyId: String? = null

    /**
     * Creates a new game lobby and registers the first player.
     * @param playerName The name of the player to register as the host.
     */
    fun setGame(playerName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val registrationResult = repository.registerPlayer(playerName)

            if (registrationResult is ApiResult.Success) {
                // Set the game ID from the successful registration
                val newGameId = registrationResult.data.gameId
                _gameId.value = newGameId
                currentLobbyId = newGameId
                // Now fetch the players for the newly created lobby
                fetchPlayers(newGameId)
            } else if (registrationResult is ApiResult.Error) {
                _uiState.value = UiState.Error(registrationResult.message)
            }
        }
    }

    fun joinGame(playerName: String, gameId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val registerResult = repository.registerPlayer(playerName, gameId)) {
                is ApiResult.Success -> {
                    // After successful registration, fetch the players for the specific game
                    fetchPlayers(gameId)
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(registerResult.message)
                }
                is ApiResult.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    private fun fetchPlayers(gameId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = repository.fetchPlayers(gameId)) {
                is ApiResult.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                is ApiResult.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    fun removePlayer(player: Player) {
        // Implementation for removing a player
    }
}
