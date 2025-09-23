package com.example.myapplication

import PlayersRepository
import android.app.Application
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bingoapp.ui.screens.JoinGameScreen
import com.example.myapplication.bingocards.BingoCardsRepository
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.ui.composable.BingoCardScreen
import com.example.myapplication.ui.screens.SetGameScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: DataViewModel = viewModel(
        factory = DataViewModelFactory(
            application = application,
            PlayersRepository(apiService = RetrofitInstance.apiService),
            bingoCardsRepository = BingoCardsRepository(apiService = RetrofitInstance.apiService)
        )
    )
    val gameStarted by viewModel.gameStarted.collectAsState()

    // Observe game state and navigate to the in-game screen
    LaunchedEffect(gameStarted) {
        if (gameStarted) {
            navController.navigate("in_game")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                navBackStackEntry?.destination?.route
                IconButton(
                    onClick = { navController.navigate("set_game") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Set Game"
                    )
                }

                IconButton(
                    onClick = { navController.navigate("join_game")  },
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
        NavHost(
            navController = navController,
            startDestination = "set_game",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("set_game") {
                SetGameScreen(viewModel = viewModel,
                    navController = navController)
            }
            composable("join_game") {
                JoinGameScreen(viewModel = viewModel)
            }
            composable("in_game") {
                BingoCardScreen(viewModel = viewModel)
            }
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
