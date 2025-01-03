package com.hume.voice.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
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
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class VoiceViewModel : ViewModel() {

    companion object {
        private const val TAG = "VoiceViewModel"
        private const val BUFFER_SIZE = 2048 // 音频缓冲区大小
    }

    private var recordingThread: Thread? = null
    private var audioRecord: AudioRecord? = null
    private var mediaPlayer: MediaPlayer? = null
    private var cacheDir: File? = null

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
                try {
                    val jsonObject = JSONObject(message)
                    val text = jsonObject.optString("text", "")
                    if (text.isNotEmpty()) {
                        _messages.value = text
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse message", e)
                }
            },
            onBinaryMessage = { audioData ->
                playAudioBytes(audioData)
            },
            onFailure = { error ->
                Log.e(TAG, "WebSocket connection failed", error)
                _messages.value = "Connection failed: ${error.message}"
            }
        )
    }

    private fun playAudioBytes(audioData: ByteArray) {
        try {
            val cache = cacheDir ?: throw IllegalStateException("Cache directory not set")

            val tempFile = File.createTempFile("audio", ".mp3", cache)
            tempFile.writeBytes(audioData)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    release()
                    tempFile.delete()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (_isRecording.value) return
        
        Log.d(TAG, "Starting recording...")
        
        val minBufferSize = AudioRecord.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        Log.d(TAG, "MinBufferSize: $minBufferSize")

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            8000, // 采样率
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed")
            return
        }

        _isRecording.value = true

        recordingThread = Thread {
            val buffer = ByteArray(BUFFER_SIZE)
            audioRecord?.startRecording()
            Log.d(TAG, "Recording thread started")

            while (_isRecording.value) {
                val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (readSize > 0) {
                    webSocketManager.sendAudioChunk(buffer.copyOf(readSize))
                }
            }

            Log.d(TAG, "Recording thread ending")
            webSocketManager.sendAudioEnd()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
        recordingThread?.start()
    }

    fun stopRecording(): Boolean {
        return try {
            _isRecording.value = false
            recordingThread?.join(1000) // 等待最多1秒
            recordingThread = null
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        audioRecord?.release()
        audioRecord = null
        mediaPlayer?.release()
        mediaPlayer = null
        webSocketManager.disconnect()
    }

    fun setCacheDir(dir: File) {
        cacheDir = dir
    }
}