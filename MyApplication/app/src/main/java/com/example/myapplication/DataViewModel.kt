package com.example.myapplication

import PlayersRepository
import SoundPlayer
import SseClient
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import okhttp3.Response

class DataViewModel(
    application: Application,
    private val playersRepository: PlayersRepository,
    private val bingoCardsRepository: BingoCardsRepository
) : AndroidViewModel(application),
    SseClient.SseListener {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId

    private val _bingoCardState = MutableStateFlow<BingoCard?>(null)
    val bingoCardState: StateFlow<BingoCard?> = _bingoCardState

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted

    private val _nextNumber = MutableStateFlow<Int?>(null)
    val nextNumber: StateFlow<Int?> = _nextNumber

    private val _playerId = MutableStateFlow<String?>(null)

    val playerId: StateFlow<String?> = _playerId
    private lateinit var sseClient: SseClient

    private var pollingJob: Job? = null

    private val soundPlayer = SoundPlayer(application.applicationContext)

    init {

        val (gameId, playerId) = playersRepository.getPlayerInfo()
        _gameId.value = gameId
        _playerId.value = playerId

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

    fun connectToBingoStream(gameId: String) {
        sseClient = SseClient(this)
        sseClient.connect(gameId)
    }

    override fun onEvent(event: String, data: String) {
        // Log the full event for debugging
        Log.d("SSETESTING", "Received event: $event, Data: $data")

        when (event) {
            "bingo_number" -> {
                try {
                    // Convert the data string to an Int and update your state
                    val number = data.toInt()
                    _nextNumber.value = number
                    soundPlayer.playSuccessSound()
                } catch (e: NumberFormatException) {
                    Log.e("DataViewModel", "onEvent: Error parsing number: $data", e)
                }
            }
            "game_over" -> {
                // Handle the game over event here, e.g., show a message to the user
                Log.d("DataViewModel", "onEvent: Game over event received.")
                // _gameStatus.value = "Game Over"
            }
            else -> {
                Log.w("SSETESTING", "Received unhandled event type: $event")
            }
        }
    }

    override fun onFailure(t: Throwable, response: Response?) {
        // Your failure handling code remains the same
        Log.e("SSETESTING", "Connection failed", t)
    }

    // Ensure you disconnect when the ViewModel is no longer needed
    override fun onCleared() {
        super.onCleared()

        playersRepository.clearPlayerInfo()

        soundPlayer.release()

        if (::sseClient.isInitialized) {
            sseClient.disconnect()
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
                    val registration = result.data
                    // Save player info using the repository
                    playersRepository.savePlayerInfo(registration.gameId, registration.playerId)
                    // Update ViewModel state
                    _gameId.value = registration.gameId
                    _playerId.value = registration.playerId
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
                    val registration = result.data
                    // Save player info using the repository
                    playersRepository.savePlayerInfo(registration.gameId, registration.playerId!!)
                    // Update ViewModel state
                    _gameId.value = registration.gameId
                    _playerId.value = registration.playerId
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



    fun onNumberClicked(number: Int) {
        viewModelScope.launch {
            when (val result = bingoCardsRepository.clickNumber(number, bingoCardState.value?.cardId)) {
                is ApiResult.Success -> {
                    if (result.data) {
                        if (uiState.value is UiState.Success) {
                            val currentPlayer = (uiState.value as UiState.Success).players.firstOrNull { it.gameId == gameId.value }
                            if (currentPlayer != null) {
                                fetchBingoCard(currentPlayer.gameId, currentPlayer.playerId)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    // Handle error state for bingo card fetching
                }
                is ApiResult.Loading -> {
                    // Handle loading state
                }
            }
        }
        //TODO("manage web service return to have better logging system on bingo cards update after click")

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
