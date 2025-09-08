package com.example.myapplication

import PlayersRepository
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bingoapp.ui.screens.JoinGameScreen
import com.example.myapplication.bingocards.BingoCardsRepository
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.ui.composable.BingoCardScreen
import com.example.myapplication.ui.screens.SetGameScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

sealed class Screen {
    object SetGame : Screen()
    object JoinGame : Screen()
    object InGame : Screen()
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SetGame) }
    val viewModel: DataViewModel = viewModel(
        factory = DataViewModelFactory(
            PlayersRepository(apiService = RetrofitInstance.apiService),
            bingoCardsRepository = BingoCardsRepository(apiService = RetrofitInstance.apiService)
        )
    )
    val gameStarted by viewModel.gameStarted.collectAsState()

    // Observe game state and navigate to the in-game screen
    LaunchedEffect(gameStarted) {
        if (gameStarted) {
            currentScreen = Screen.InGame
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                IconButton(
                    onClick = { currentScreen = Screen.SetGame },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Set Game"
                    )
                }

                IconButton(
                    onClick = { currentScreen = Screen.JoinGame },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Join Game"
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            is Screen.SetGame -> SetGameScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
            is Screen.JoinGame -> JoinGameScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
            is Screen.InGame -> BingoCardScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Text("Android")
    }
}
