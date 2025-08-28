/*package com.example.myapplication.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.DataViewModel
import com.example.myapplication.DataViewModelFactory
import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.network.ServerRepository

@Composable
fun SubmitDataScreen(viewModel: DataViewModel = viewModel()) {
    Column {
        Text("Click the button to send a message to the server.")

        Button(onClick = {  }) {
            Text("Send Message")
        }
    }
}
@Composable
fun DataScreen(modifier : Modifier) {
    // 1. Instantiate the repository.
    val repository = ServerRepository(apiService = RetrofitInstance.apiService)

    // 2. Instantiate the ViewModel using the factory.
    val viewModel: DataViewModel = viewModel(
        factory = DataViewModelFactory(repository)
    )

    // 3. Observe the UI state from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // 4. Render the UI based on the state (Loading, Success, or Error).
    when (uiState) {
        is ApiResult.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is ApiResult.Success -> {
            val data = (uiState as ApiResult.Success).data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Message: ${data.message}")
                Text(text = "Count: ${data.messagesReceived.size}")
            }
        }
        is ApiResult.Error -> {
            val errorMessage = (uiState as ApiResult.Error).message
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Error: $errorMessage")
                Button(onClick = { viewModel.fetchData() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Retry")
                }
            }
        }
    }
    SubmitDataScreen()
}*/