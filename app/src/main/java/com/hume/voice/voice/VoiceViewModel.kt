package com.hume.voice.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hume.voice.common.config.AppConfig
import com.hume.voice.common.network.WebSocketManager

class VoiceViewModel : ViewModel() {

    companion object {
        private const val TAG = "VoiceViewModel"
        private const val CONFIG_ID = "8bf90e11-253b-43e6-a0ba-c298e3351b49"
        private const val RESUMED_CHAT_GROUP_ID = "929c8801-0cb4-40be-92ec-294b588697f3"
    }

    private val webSocketManager = WebSocketManager()

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        webSocketManager.connect(
            apiKey = AppConfig.API_KEY,
            configId = CONFIG_ID,
            resumedChatGroupId = RESUMED_CHAT_GROUP_ID
        ) { message ->
            Log.d(TAG, "Received WebSocket message: $message")
        }
    }

    fun sendAudioData(base64Audio: String) {
        webSocketManager.sendAudioData(base64Audio)
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}