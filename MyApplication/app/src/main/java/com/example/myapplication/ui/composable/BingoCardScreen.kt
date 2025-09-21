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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Bingo Card",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Game ID: $gameId",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        when (bingoCardState) {
            null -> {
                Text("Loading your card...")
            }
            else -> {
                BingoCardGrid(
                    card = bingoCardState!!.card,
                    onNumberClick = { number ->
                        if (number != null) {
                            viewModel.onNumberClicked(number)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BingoCardGrid(
    card: List<List<Int?>>,
    onNumberClick: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        card.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                row.forEach { number ->
                    BingoSquare(
                        modifier = Modifier.weight(1f)
                                .aspectRatio(1f) // Ensures the button is square
                                .padding(1.dp),
                        number = number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }
    }
}

@Composable
fun BingoSquare(
    number: Int?,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape, // Sets the button shape to a square
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray
        )
    ) {
        Text(
            text = when {
                number == -1 -> "\u2713"
                number != null -> number.toString()
                else -> "\u2605"
            },
            textAlign = TextAlign.Center
        )
    }
}