package com.example.myapplication.ui.utils

import com.example.myapplication.player.Player

sealed class UiState {
    object Loading : UiState()
    data class Success(val players: List<Player>) : UiState()
    data class Error(val message: String) : UiState()
}