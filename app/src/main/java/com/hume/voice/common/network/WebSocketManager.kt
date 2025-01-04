package com.hume.voice.common.network

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val BASE_WS_URL = "wss://api.hume.ai/v0/evi/chat"
        private const val BUFFER_INTERVAL = 20L
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val isStreaming = AtomicBoolean(false)

    fun connect(
        apiKey: String,
        configId: String,
        resumedChatGroupId: String,
        onMessage: (String) -> Unit
    ) {
        val url = "$BASE_WS_URL?api_key=$apiKey&config_id=$configId&resumed_chat_group_id=$resumedChatGroupId"
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection established")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
            }
        })
    }

    fun startAudioStream() {
        isStreaming.set(true)
    }

    fun stopAudioStream() {
        isStreaming.set(false)
    }

    fun sendAudioData(base64Audio: String) {
        if (!isStreaming.get()) {
            startAudioStream()
        }

        coroutineScope.launch {
            try {
                val chunkSize = 1024
                var offset = 0
                
                while (offset < base64Audio.length && isStreaming.get()) {
                    val endIndex = minOf(offset + chunkSize, base64Audio.length)
                    val chunk = base64Audio.substring(offset, endIndex)
                    
                    val jsonMessage = JSONObject().apply {
                        put("type", "audio_input")
                        put("data", chunk)
                    }
                    
                    webSocket?.send(jsonMessage.toString())
                    offset += chunkSize
                    
                    delay(BUFFER_INTERVAL)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending audio stream: ${e.message}")
            }
        }
    }

    fun disconnect() {
        stopAudioStream()
        coroutineScope.cancel()
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }
}