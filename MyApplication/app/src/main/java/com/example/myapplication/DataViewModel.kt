package com.example.myapplication

// This ViewModel is responsible for getting the data from the repository
// and exposing the UI state to the Composable.
import PlayersRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.ApiResult
import com.example.myapplication.player.Player
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel(private val repository: PlayersRepository) : ViewModel() {

    // Expose the players list from the repository as a StateFlow
    val players: StateFlow<List<Player>> = repository.players

    init {
        viewModelScope.launch {
            repository.fetchPlayers()
        }
    }

    // Function to add a player (triggers the network call)
    fun addPlayer(playerName: String) {
        viewModelScope.launch {
            if (playerName.isNotBlank()) {
                val result = repository.registerPlayer(playerName)
                // You can add logic here to handle success or error messages
                if (result is ApiResult.Success) {
                    repository.fetchPlayers()
                }
            }
        }
    }

    /*TODO implement*/
    fun removePlayer(player: Player) {
        // You would implement a network call to the server to remove the player
    }
}