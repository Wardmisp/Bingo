// SseClient.kt
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SseClient(private val coroutineScope: CoroutineScope) {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val isConnected = AtomicBoolean(false)
    private var currentCall: okhttp3.Call? = null

    // Flux pour les événements SSE
    private val _events = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    val events: SharedFlow<Pair<String, String>> = _events.asSharedFlow()

    // État de la connexion
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String, val response: Response? = null) : ConnectionState()
    }

    fun connect(gameId: String) {
        if (isConnected.get()) return
        isConnected.set(true)
        _connectionState.value = ConnectionState.Connecting

        val request = Request.Builder()
            .url("https://bingo-jl6k.onrender.com/bingo-stream/$gameId")
            .header("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")
            .build()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                currentCall = client.newCall(request)
                currentCall?.execute()?.use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Unexpected code ${response.code}")
                    }

                    _connectionState.value = ConnectionState.Connected
                    val reader = BufferedReader(InputStreamReader(response.body?.byteStream()))
                    var line = ":"
                    var currentEvent = "message"
                    var currentData = StringBuilder()

                    while (isConnected.get() && reader.readLine().also { line = it } != null) {
                        when {
                            line!!.startsWith(":") -> continue
                            line!!.startsWith("event:") -> currentEvent = line!!.substring(7).trim()
                            line!!.startsWith("data:") -> currentData.append(line!!.substring(5).trim())
                            line!!.isEmpty() -> {
                                if (currentData.isNotEmpty()) {
                                    _events.emit(Pair(currentEvent, currentData.toString()))
                                    currentData = StringBuilder()
                                    currentEvent = "message"
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error", null)
            } finally {
                isConnected.set(false)
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    fun disconnect() {
        isConnected.set(false)
        currentCall?.cancel()
    }
}
