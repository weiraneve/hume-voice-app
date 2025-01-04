package com.hume.voice.voice

import androidx.lifecycle.ViewModel
import com.hume.voice.common.obj.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HumeViewModel : ViewModel() {

    private val _webUrl = MutableStateFlow(Constants.HUME_BASE_URL)
    val webUrl: StateFlow<String> = _webUrl

    companion object {
        private const val TAG = "HumeViewModel"
    }

}