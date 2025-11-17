package com.example.myapplication.ui.composable

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun BingoCardScreen(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel()
) {
    // États collectés
    val uiState by viewModel.uiState.collectAsState()
    val bingoCardState by viewModel.bingoCardState.collectAsState()
    val gameId by viewModel.gameId.collectAsState()
    val nextNumber by viewModel.nextNumber.collectAsState()
    val currentPlayerId by viewModel.playerId.collectAsState()
    val gameStatusMessage by viewModel.gameStatusMessage.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    // Connexion automatique au flux SSE quand le gameId est disponible
    LaunchedEffect(gameId, currentPlayerId) {
        if (gameId != null && currentPlayerId != null) {
            viewModel.fetchBingoCardForPlayerId(gameId!!, currentPlayerId!!)
            viewModel.connectToBingoStream(gameId!!)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête avec ID de jeu et statut de connexion
        Text(
            text = "Your Bingo Card",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Game ID: ${gameId ?: "N/A"}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        // Indicateur de statut de connexion
        when (val state = connectionState) {
            SseClient.ConnectionState.Connecting -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting to game stream...")
                }
            }
            is SseClient.ConnectionState.Error -> {
                Text(
                    text = "Connection error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            SseClient.ConnectionState.Disconnected -> {
                Text(
                    text = "Disconnected from game stream",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            else -> { /* Connected - nothing to show */ }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage de la carte de bingo
        when (val cardState = bingoCardState) {
            null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading your card...")
                }
            }
            else -> {
                BingoCardGrid(
                    card = cardState.card,
                    onNumberClick = { number ->
                        number?.let { viewModel.onNumberClicked(it) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Zone d'affichage du nombre courant ou des messages
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Message de statut de jeu (ex: game over)
                gameStatusMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = gameStatusMessage!!,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Nombre courant tiré
                nextNumber != null -> {
                    Text(
                        text = nextNumber.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                            .padding(24.dp)
                    )
                }

                // En attente du premier nombre
                else -> {
                    Text(
                        text = "Waiting for first number...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun BingoCardGrid(
    card: List<List<Int?>>,
    onNumberClick: (Int?) -> Unit,
    modifier: Modifier
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