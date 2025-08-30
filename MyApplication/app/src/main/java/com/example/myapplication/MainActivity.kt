package com.example.myapplication

import JoinGameScreen
import PlayersRepository
import SetGameScreen
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.ui.theme.MyApplicationTheme

sealed class Screen {
    object SetGame : Screen()
    object JoinGame : Screen()
}

@Composable
fun MainScreen() {
    // State to hold the currently selected screen
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SetGame) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                // Button to switch to SetGameScreen
                IconButton(
                    onClick = { currentScreen = Screen.SetGame },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Set Game"
                    )
                }

                // Button to switch to JoinGameScreen
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
        val viewModel: DataViewModel = viewModel(
            factory = DataViewModelFactory(PlayersRepository(apiService = RetrofitInstance.apiService))
        )

        // Display the correct screen based on the state
        when (currentScreen) {
            is Screen.SetGame -> SetGameScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
            is Screen.JoinGame -> JoinGameScreen(
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}