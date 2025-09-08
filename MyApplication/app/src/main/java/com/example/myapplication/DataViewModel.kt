package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PlayersRepository
import com.example.myapplication.network.ApiResult
import com.example.myapplication.player.Player
import com.example.myapplication.ui.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataViewModel(private val repository: PlayersRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Collect from the gameId flow to automatically start/stop polling
        viewModelScope.launch {
            _gameId.collectLatest { id ->
                // Cancel any previous polling job
                pollingJob?.cancel()

                // If a gameId exists, start a new polling job
                if (id != null) {
                    startPollingForPlayers(id)
                }
            }
        }
    }

    /**
     * Creates a new game lobby and registers the first player.
     * @param playerName The name of the player to register as the host.
     */
    fun setGame(playerName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.createGame(playerName)) {
                is ApiResult.Success -> {
                    // Set the game ID from the successful registration
                    _gameId.value = result.data.gameId
                    // The polling mechanism will automatically fetch the players list
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                // No loading state to handle here as it's set at the beginning
                ApiResult.Loading -> { }
            }
        }
    }

    /**
     * Adds a new player to an existing game.
     * @param playerName The name of the player to join.
     * @param gameId The ID of the game to join.
     */
    fun joinGame(playerName: String, gameId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val registerResult = repository.joinGame(playerName, gameId)) {
                is ApiResult.Success -> {
                    // Set the gameId which will trigger the polling mechanism
                    _gameId.value = gameId
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(registerResult.message)
                }
                // No loading state to handle here as it's set at the beginning
                ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Fetches the players for a given game ID and updates the UI state.
     * This function is now called repeatedly by the polling job.
     * @param gameId The ID of the game.
     */
    private fun fetchPlayers(gameId: String) {
        viewModelScope.launch {
            when (val result = repository.fetchPlayers(gameId)) {
                is ApiResult.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    // Cancel the polling job on error to prevent constant failed requests
                    pollingJob?.cancel()
                }
                // No loading state to handle here
                ApiResult.Loading -> { }
            }
        }
    }

    /**
     * New private function to handle continuous polling for players.
     */
    private fun startPollingForPlayers(gameId: String) {
        pollingJob = viewModelScope.launch {
            while (true) {
                // Fetch players and update UI state
                fetchPlayers(gameId)
                // Wait for 3 seconds before the next update
                delay(3000L)
            }
        }
    }

    fun removePlayer(player: Player) {
        // Implementation for removing a player
    }

    // You can add this for cleanup, though viewModelScope handles it automatically
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
