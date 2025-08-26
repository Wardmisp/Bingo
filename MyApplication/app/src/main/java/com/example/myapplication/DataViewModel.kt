package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.network.ServerRepository
import com.example.myapplication.network.ApiResult
import com.example.myapplication.network.ReceivedDataModel

// This ViewModel is responsible for getting the data from the repository
// and exposing the UI state to the Composable.
class DataViewModel(private val repository: ServerRepository) : ViewModel() {

    // MutableStateFlow to hold the current state of our API call.
    // It's private so only the ViewModel can change its value.
    private val _uiState = MutableStateFlow<ApiResult<ReceivedDataModel>>(ApiResult.Loading)

    // StateFlow to be exposed to the UI. The UI observes this.
    val uiState: StateFlow<ApiResult<ReceivedDataModel>> = _uiState

    init {
        fetchData()
    }

    // Function to trigger the network request.
    fun fetchData() {
        // Launch a coroutine in the ViewModel's scope.
        viewModelScope.launch {
            // Update the state to Loading before the network call.
            _uiState.value = ApiResult.Loading

            // Call the repository function and update the state based on the result.
            val result = repository.fetchData()
            _uiState.value = result
        }
    }
}