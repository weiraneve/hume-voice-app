package com.hume.voice.common.network

import android.util.Base64
import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
    }

    private var webSocket: WebSocket? = null

    fun connect(
        request: Request,
        client: OkHttpClient,
        onMessage: (String) -> Unit,
        onBinaryMessage: (ByteArray) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened successfully")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received text message: $text")
                onMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "Received binary message, size: ${bytes.size}")
                onBinaryMessage(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                onFailure(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }

    fun sendAudioChunk(audioData: ByteArray) {
        try {
            val base64Data = Base64.encodeToString(audioData, Base64.NO_WRAP)
            val message = JSONObject().apply {
                put("type", "audio_input")
                put("data", base64Data)
            }
            webSocket?.send(message.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send audio chunk", e)
        }
    }

    fun sendAudioEnd() {
        try {
            val message = JSONObject().apply {
                put("type", "audio_input")
                put("data", "")
            }
            Log.d(TAG, "Sending end stream message")
            Log.d(TAG, "End Message structure: $message")
            webSocket?.send(message.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send audio end marker", e)
        }
    }
}