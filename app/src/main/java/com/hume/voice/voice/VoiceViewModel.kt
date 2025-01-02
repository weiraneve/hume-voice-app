package com.hume.voice.voice

import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hume.voice.common.config.AppConfig
import com.hume.voice.common.network.WebSocketManager
import com.hume.voice.common.obj.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class VoiceViewModel : ViewModel() {

    companion object {
        private const val TAG = "VoiceViewModel"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val webSocketManager = WebSocketManager()
    private val _messages = MutableStateFlow<String>("")
    val messages = _messages.asStateFlow()

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val wsUrl = Constants.WS_BASE_URL +
                "?api_key=${AppConfig.API_KEY}" +
                "&config_id=${Constants.CONFIG_ID}" +
                "&resumed_chat_group_id=${Constants.CHAT_GROUP_ID}"

        Log.d(TAG, "Connecting to WebSocket URL: $wsUrl")

        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocketManager.connect(
            request = request,
            client = client,
            onMessage = { message ->
                // ... existing message handling code ...
            },
            onFailure = { error ->
                Log.e(TAG, "WebSocket connection failed", error)
                _messages.value = "Connection failed: ${error.message}"
            }
        )
    }

    fun startRecording(cacheDir: File) {
        if (_isRecording.value) return

        audioFile = File(cacheDir, "audio_record.mp3")

        @Suppress("DEPRECATION")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }

        _isRecording.value = true
    }

    fun stopRecording(): Boolean {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false

            return audioFile?.let { file ->
                val isValid = file.exists() && file.length() > 0
                Log.d(
                    TAG, """
                            录音文件信息:
                            路径: ${file.absolutePath}
                            大小: ${file.length()} bytes
                        """.trimIndent()
                )

                if (isValid) {
                    webSocketManager.sendAudioFile(file)
                } else {
                    file.delete()
                }
                isValid
            } == true

        } catch (e: Exception) {
            Log.e(TAG, "录音停止失败", e)
            audioFile?.delete()
            return false
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
        webSocketManager.disconnect()
    }
}