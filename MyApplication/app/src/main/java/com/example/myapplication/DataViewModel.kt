package com.example.myapplication

import PlayersRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.bingocards.BingoCard
import com.example.myapplication.bingocards.BingoCardsRepository
import com.example.myapplication.network.ApiResult
import com.example.myapplication.player.Player
import com.example.myapplication.ui.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataViewModel(
    private val playersRepository: PlayersRepository,
    private val bingoCardsRepository: BingoCardsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId

    private val _bingoCardState = MutableStateFlow<BingoCard?>(null)
    val bingoCardState: StateFlow<BingoCard?> = _bingoCardState

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            _gameId.collectLatest { gameId ->
                pollingJob?.cancel()
                if (gameId != null) {
                    pollingJob = launch {
                        while (true) {
                            fetchPlayers(gameId)
                            delay(3000) // Poll every 3 seconds
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchPlayers(gameId: String) {
        when (val result = playersRepository.fetchPlayers(gameId)) {
            is ApiResult.Success -> {
                val players = result.data
                _uiState.value = UiState.Success(players)
                // Check if the game has started
                val hostPlayer = players.firstOrNull { it.isHost }
                if (hostPlayer?.gameStarted == true) {
                    _gameStarted.value = true
                }
            }
            is ApiResult.Error -> {
                _uiState.value = UiState.Error(result.message)
            }
            is ApiResult.Loading -> {
                _uiState.value = UiState.Loading
            }
        }
    }

    fun createGame(playerName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = playersRepository.createGame(playerName)) {
                is ApiResult.Success -> {
                    _gameId.value = result.data.gameId
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

    fun joinGame(playerName: String, gameId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = playersRepository.joinGame(playerName, gameId)) {
                is ApiResult.Success -> {
                    _gameId.value = result.data.gameId
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

    fun launchGame() {
        // Implementation for launching the game
    }

    fun removePlayer(player: Player) {
        // Implementation for removing a player
    }

    fun fetchBingoCard(gameId: String, playerId: String) {
        viewModelScope.launch {
            when (val result = bingoCardsRepository.fetchBingoCard(gameId, playerId)) {
                is ApiResult.Success -> {
                    _bingoCardState.value = result.data
                }
                is ApiResult.Error -> {
                    // Handle error state for bingo card fetching
                }
                is ApiResult.Loading -> {
                    // Handle loading state
                }
            }
        }
    }
}
