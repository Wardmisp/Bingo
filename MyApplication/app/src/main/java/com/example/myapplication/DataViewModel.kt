package com.example.myapplication

import PlayersRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.ApiResult
import com.example.myapplication.player.Player
import com.example.myapplication.ui.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataViewModel(private val repository: PlayersRepository) : ViewModel() {

    // Internal mutable StateFlow
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    // Public immutable StateFlow for the UI to observe
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        fetchPlayers()
    }

    // Function to fetch all players from the repository
    fun fetchPlayers() {
        viewModelScope.launch {
            // Set the state to Loading before the network call
            _uiState.value = UiState.Loading

            val result = repository.fetchPlayers() // Assuming a getPlayers() function in your repository

            when (result) {
                is ApiResult.Success -> {
                    // Update the state to Success with the list of players
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    // Update the state to Error with the error message
                    _uiState.value = UiState.Error(result.message)
                }

                ApiResult.Loading -> {
                // Update the state to Loading
                _uiState.value = UiState.Loading
            }
            }
        }
    }

    // Function to add a player (triggers the network call)
    fun addPlayer(playerName: String) {
        viewModelScope.launch {
            if (playerName.isNotBlank()) {
                val result = repository.registerPlayer(playerName)
                if (result is ApiResult.Success) {
                    // Refresh the UI by fetching the players again
                    fetchPlayers()
                } else if (result is ApiResult.Error) {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }

    /*TODO implement*/
    fun removePlayer(player: Player) {
        // You would implement a network call to the server to remove the player
    }
}