package com.example.myapplication.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.DataViewModel
import com.example.myapplication.ui.utils.UiState

@Composable
fun BingoCardScreen(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bingoCardState by viewModel.bingoCardState.collectAsState()
    val gameId by viewModel.gameId.collectAsState()

    LaunchedEffect(Unit) {
        val currentState = uiState
        if (currentState is UiState.Success) {
            val currentPlayer = currentState.players.firstOrNull { it.gameId == gameId }
            if (currentPlayer != null) {
                viewModel.fetchBingoCard(currentPlayer.gameId, currentPlayer.playerId)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Bingo Card",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        when (bingoCardState) {
            null -> {
                Text("Loading your card...")
            }
            else -> {
                Text(text = "Bingo Card: ${bingoCardState?.card}")
            }
        }
    }
}
