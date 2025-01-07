package com.hume.voice.modules

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hume.voice.common.obj.Constants
import com.hume.voice.common.obj.HumeMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HumeViewModel() : ViewModel() {

    companion object {
        private const val TAG = "HumeViewModel"
    }

    private val _webUrl = MutableStateFlow(Constants.HUME_BASE_URL)
    val webUrl = _webUrl.asStateFlow()

    private val _messages = MutableStateFlow<List<HumeMessage>>(emptyList())
    val messages: StateFlow<List<HumeMessage>> = _messages.asStateFlow()

    fun handleWebSocketMessage(message: HumeMessage) {
        viewModelScope.launch {
            when (message.type) {
                "assistant_message" -> {
                    _messages.value = _messages.value + message

                    if (message.message.content.isNotEmpty()) {
                        Log.d(TAG, message.message.content)
                    }
                }

                "user_message" -> {
                    _messages.value = _messages.value + message

                    if (message.message.content.isNotEmpty()) {
                        Log.d(TAG, message.message.content)
                    }
                }
            }
        }
    }
}