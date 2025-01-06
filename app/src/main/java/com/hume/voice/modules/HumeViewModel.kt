package com.hume.voice.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hume.voice.common.obj.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HumeViewModel : ViewModel() {

    private val _webUrl = MutableStateFlow(Constants.HUME_BASE_URL)
    val webUrl = _webUrl.asStateFlow()

    private val _callStatus = MutableStateFlow<CallStatus>(CallStatus.Disconnected)
    val callStatus: StateFlow<CallStatus> = _callStatus.asStateFlow()

    fun handleCallStart() {
        viewModelScope.launch {
            _callStatus.value = CallStatus.Connecting
            // 处理通话开始逻辑
        }
    }
}

sealed class CallStatus {
    object Disconnected : CallStatus()
    object Connecting : CallStatus()
    object Connected : CallStatus()
}