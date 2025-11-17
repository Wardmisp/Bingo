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
import com.example.myapplication.gamestatus.GameRepository
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
    application: Application,
    private val playersRepository: PlayersRepository,
    private val bingoCardsRepository: BingoCardsRepository,
    private val gameRepository: GameRepository,
) : AndroidViewModel(application) {
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

    private val _gameStatusMessage = MutableStateFlow<String?>(null)
    val gameStatusMessage: StateFlow<String?> = _gameStatusMessage

    // État de la connexion SSE
    val connectionState: StateFlow<SseClient.ConnectionState> get() = sseClient.connectionState

    private lateinit var sseClient: SseClient
    private var pollingJob: Job? = null
    private val soundPlayer = SoundPlayer(application.applicationContext)

    init {
        sseClient = SseClient(viewModelScope) // Initialisation ici

        // Collecte des événements SSE
        viewModelScope.launch {
            sseClient.events.collect { (event, data) ->
                Log.d("SSETESTING", "Received event: $event, Data: $data")
                when (event) {
                    "bingo_number" -> {
                        try {
                            val cleanData = data.removePrefix("b'").removeSuffix("'")
                            val number = cleanData.toInt()
                            _nextNumber.value = number
                            soundPlayer.playSuccessSound()
                        } catch (e: NumberFormatException) {
                            Log.e("DataViewModel", "onEvent: Error parsing number: $data", e)
                        }
                    }
                    "game_over" -> {
                        Log.d("DataViewModel", "Game over event received with data: $data")
                        _gameStatusMessage.value = data
                    }
                    else -> {
                        Log.w("SSETESTING", "Received unhandled event type: $event")
                    }
                }
            }
        }

        // Collecte des changements d'état de connexion
        viewModelScope.launch {
            sseClient.connectionState.collect { state ->
                when (state) {
                    is SseClient.ConnectionState.Error -> {
                        Log.e("SSETESTING", "Connection failed: ${state.message}")
                        // Reconnecter après un délai
                        delay(5000)
                        _gameId.value?.let { gameId -> connectToBingoStream(gameId) }
                    }
                    else -> {
                        // Optionnel: log des autres états
                        Log.d("SSETESTING", "Connection state changed: $state")
                    }
                }
            }
        }

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
        sseClient.connect(gameId)
    }

    // Le reste de ton code existant reste inchangé...
    override fun onCleared() {
        super.onCleared()
        playersRepository.clearPlayerInfo()
        soundPlayer.release()
        sseClient.disconnect()
    }

    private suspend fun fetchPlayers(gameId: String) {
        when (val result = playersRepository.fetchPlayers(gameId)) {
            is ApiResult.Success -> {
                val players = result.data
                _uiState.value = UiState.Success(players)
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
                    playersRepository.savePlayerInfo(registration.gameId, registration.playerId)
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
                    playersRepository.savePlayerInfo(registration.gameId, registration.playerId)
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
                            if (playerId.value != null) {
                                fetchBingoCardByCardId(bingoCardState.value?.cardId!!)
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
    }

    fun launchGame() {
        viewModelScope.launch {
            when (gameRepository.launchGame(_gameId.value!!)) {
                is ApiResult.Error -> Log.i("BingoInfo", "launchGame: failed")
                ApiResult.Loading -> Log.i("BingoInfo", "launchGame: loading")
                is ApiResult.Success<*> -> Log.i("BingoInfo", "launchGame: a game has been launched!")
            }
        }
    }

    fun removePlayer(player: Player) {
        playersRepository.clearPlayerInfo()
        // TODO("IMPLEMENT REMOVE PLAYER SERVER SIDE")
    }

    fun fetchBingoCardForPlayerId(gameId: String, playerId: String) {
        viewModelScope.launch {
            when (val result = bingoCardsRepository.fetchBingoCardForPlayerId(gameId, playerId)) {
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

    fun fetchBingoCardByCardId(cardId: String) {
        viewModelScope.launch {
            when (val result = bingoCardsRepository.fetchBingoCardByCardId(cardId)) {
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
