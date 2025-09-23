package com.example.myapplication.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.DataViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.utils.UiState

@Composable
fun SetGameScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: DataViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var playerName by remember { mutableStateOf("") }
    val isPlayerNameValid = playerName.isNotBlank()
    val playersCount = (uiState as? UiState.Success)?.players?.size ?: 0
    val isGameStarted by viewModel.gameStarted.collectAsState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dynamic content based on UI state
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                is UiState.Success -> {
                    val players = (uiState as UiState.Success).players
                    val gameId by viewModel.gameId.collectAsState()

                    // Display game ID at the top
                    Text(
                        text = "Game ID: $gameId",
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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
                                Text(
                                    text = "No players found. Add one!",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    val errorMessage = (uiState as UiState.Error).message
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                is UiState.Initial -> {
                    Text(
                        text = "Welcome to the bingo! Enter your name to create a game.",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player adding UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Add Player Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isGameStarted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Start game" button with dynamic label
        val buttonText = when {
            playersCount == 0 -> "Create new game"
            else -> "Launch the game"
            //else -> "Launch the game"
            // @T0DO(DONT FORGET WAITING FOR PLAYERS)
        }

        Button(
            onClick = {
                if (playersCount == 0) {
                    viewModel.createGame(playerName)
                } else {
                    viewModel.launchGame()
                    navController.navigate("in_game")
                }
            },
            enabled = (isPlayerNameValid && playersCount == 0) || (playersCount >= 1 && !isGameStarted),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(buttonText)
        }
    }
}
