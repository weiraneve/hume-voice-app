package com.hume.voice.modules

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hume.voice.common.obj.Constants
import com.hume.voice.common.obj.HumeMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HumeViewModel(application: Application) : ViewModel() {

    companion object {
        private const val TAG = "HumeViewModel"
    }

    private val _webUrl = MutableStateFlow(Constants.HUME_BASE_URL)
    val webUrl = _webUrl.asStateFlow()

    private val _messages = MutableStateFlow<List<HumeMessage>>(emptyList())
    val messages: StateFlow<List<HumeMessage>> = _messages.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext


    fun handleWebSocketMessage(message: HumeMessage) {
        viewModelScope.launch {
            when (message.type) {
                "assistant_message" -> {
                    _messages.value = _messages.value + message

                    if (message.message.content.isNotEmpty()) {
                        Log.d(TAG, message.message.content)
                        Toast.makeText(context, message.message.content, Toast.LENGTH_SHORT).show()
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