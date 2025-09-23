package com.example.bingoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.DataViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.utils.UiState

@Composable
fun JoinGameScreen(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var playerName by remember { mutableStateOf("") }
    var gameId by remember { mutableStateOf("") }
    val isGameIdValid = gameId.isNotBlank() && gameId.all { it.isDigit() }
    val isPlayerNameValid = playerName.isNotBlank()
    val playersCount = (uiState as? UiState.Success)?.players?.size ?: 0

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main content area that changes based on the UI state
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    // Show a loading indicator while the data is being fetched
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                is UiState.Success -> {
                    // Cast the state to UiState.Success to access the players list
                    val players = (uiState as UiState.Success).players

                    // "Board" for player names
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .padding(8.dp)
                    ) {
                        if (players.isNotEmpty()) {
                            items(players) { player ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = player.name,
                                            fontSize = 18.sp,
                                        )
                                        IconButton(
                                            onClick = { viewModel.removePlayer(player) }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.baseline_clear_24),
                                                contentDescription = "Remove Player",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                // Show a message when the list is empty
                                Text(text = "No players found. Add one!")
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    // Show an error message if the API call failed
                    val errorMessage = (uiState as UiState.Error).message
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    // Show the initial prompt when no game is joined
                    Text(
                        text = "Type a lobby ID and your name to enter!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (playersCount == 0) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = gameId,
                    onValueChange = { gameId = it },
                    label = { Text("Enter game ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isGameIdValid && gameId.isNotBlank(),
                    supportingText = {
                        if (!isGameIdValid && gameId.isNotBlank()) {
                            Text("Game ID must be a number")
                        }
                    },
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Enter your name") },
                    isError = playerName.isBlank() && gameId.isNotBlank(),
                    supportingText = {
                        if (playerName.isBlank() && gameId.isNotBlank()) {
                            Text("Name cannot be empty")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // "Start game" button
            Button(
                onClick = { viewModel.joinGame(playerName, gameId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isPlayerNameValid && isGameIdValid
            ) {
                Text("Join game")
            }
        } else {

            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Waiting for host to start the game...",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )

        }
    }
}
