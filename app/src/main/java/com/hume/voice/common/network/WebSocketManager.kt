package com.hume.voice.common.network

import android.util.Base64
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.File

class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
    }

    private var webSocket: WebSocket? = null

    fun connect(
        request: Request,
        client: OkHttpClient,
        onMessage: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connection opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }
        })
    }

    fun sendAudioFile(file: File) {
        try {
            val audioData = file.readBytes()
            val base64Data = Base64.encodeToString(audioData, Base64.NO_WRAP)

            val message = JSONObject().apply {
                put("type", "audio_input")
                put("data", base64Data)
            }

            webSocket?.send(message.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send audio file", e)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }
}