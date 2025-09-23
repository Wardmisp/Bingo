import okhttp3.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class SseClient(private val listener: SseListener) {

    private val client = OkHttpClient.Builder().build()
    private val executor = Executors.newSingleThreadExecutor()
    private var isConnected = false

    interface SseListener {
        fun onEvent(event: String, data: String)
        fun onFailure(t: Throwable, response: Response?)
    }

    fun connect(gameId: String) {
        if (isConnected) return
        isConnected = true

        val request = Request.Builder()
            .url("https://bingo-jl6k.onrender.com/bingo-stream/$gameId")
            .header("Accept", "text/event-stream") // Set the correct MIME type
            .build()

        // Use a background thread to read the stream
        executor.execute {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        listener.onFailure(
                            Exception("Unexpected code $response"),
                            response
                        )
                        isConnected = false
                        return@execute
                    }

                    val reader = BufferedReader(InputStreamReader(response.body.byteStream()))
                    var line: String?
                    var event = "message"
                    var data = ""

                    while (reader.readLine().also { line = it } != null && isConnected) {
                        when {
                            line!!.startsWith("event:") -> {
                                event = line.substring("event:".length).trim()
                            }
                            line.startsWith("data:") -> {
                                data = line.substring("data:".length).trim()
                            }
                            line.isEmpty() -> { // End of a full event message
                                if (data.isNotEmpty()) {
                                    listener.onEvent(event, data)
                                    // Reset for the next message
                                    event = "message"
                                    data = ""
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                listener.onFailure(e, null)
                isConnected = false
            }
        }
    }

    fun disconnect() {
        isConnected = false
        executor.shutdownNow()
    }
}