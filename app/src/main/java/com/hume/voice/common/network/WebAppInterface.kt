package com.hume.voice.common.network

import android.util.Log
import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.hume.voice.common.obj.HumeMessage
import com.hume.voice.modules.HumeViewModel

class WebAppInterface(private val viewModel: HumeViewModel) {
    companion object {
        private const val TAG = "WebAppInterface"
    }

    @JavascriptInterface
    fun onMessageReceived(messageJson: String) {
        try {
            Log.d(TAG, "Received raw JSON: $messageJson")
            val message = Gson().fromJson(messageJson, HumeMessage::class.java)
            viewModel.handleWebSocketMessage(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message", e)
            e.printStackTrace()
        }
    }
}