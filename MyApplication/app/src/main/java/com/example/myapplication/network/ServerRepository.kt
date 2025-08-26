package com.example.myapplication.network
import retrofit2.Response

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()

    data class Error(val message: String) : ApiResult<Nothing>()

    object Loading : ApiResult<Nothing>()
}

class ServerRepository(private val apiService: ApiService) {
    // The suspend keyword indicates that this function is a coroutine.
    // It can be called from a coroutine and won't block the main thread.
    suspend fun fetchData(): ApiResult<ReceivedDataModel> {
        return try {
            val response = apiService.getData()
            if (response.isSuccessful) {
                // If the request was successful, wrap the data in a Success object.
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Response body is empty.")
            } else {
                // If the response code indicates an error, wrap the error message.
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // Catch any network or other exceptions.
            ApiResult.Error("Network Error: ${e.message}")
        }
    }

    // New function to handle sending data to the server
    suspend fun submitData(submission: Submission): ApiResult<Unit> {
        return try {
            val response = apiService.submitData(submission)
            if (response.isSuccessful) {
                // If the submission was successful, return a Success result.
                // We use Unit here since the server's response has no body.
                ApiResult.Success(Unit)
            } else {
                // If the response code indicates an error, wrap the error message.
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // Catch any network or other exceptions.
            ApiResult.Error("Network Error: ${e.message}")
        }
    }
}