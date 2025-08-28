package com.example.myapplication.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.DataViewModel
import com.example.myapplication.R

@Composable
fun SetGameScreen(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel()
) {
    val players by viewModel.players.collectAsState()
    var playerName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth() // Added fillMaxWidth for proper horizontal alignment
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // This centers all children vertically
    ) {
        // "Board" for player names
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // Gives the board a maximum height
                .padding(8.dp)
        ) {
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
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    viewModel.addPlayer(playerName)
                    playerName = ""
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_circle_outline_24),
                    contentDescription = "Add Player",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Start game" button
        Button(
            onClick = { /* TODO: Implement game start logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Start game")
        }
    }
}

@Preview
@Composable
fun SetGameScreenPreview() {
    SetGameScreen()
}