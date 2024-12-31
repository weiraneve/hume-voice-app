package com.hume.voice.voice

import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class VoiceViewModel : ViewModel() {

    companion object {
        private const val TAG = "VoiceViewModel"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

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

            // 检查录音文件是否有效
            return audioFile?.let { file ->
                val isValid = file.exists() && file.length() > 0
                Log.d(
                    TAG, """
                    录音文件信息:
                    路径: ${file.absolutePath}
                    是否存在: ${file.exists()}
                    文件大小: ${file.length()} bytes
                    最后修改时间: ${java.util.Date(file.lastModified())}
                    是否可读: ${file.canRead()}
                    是否有效: $isValid
                """.trimIndent()
                )

                if (!isValid) {
                    file.delete()
                    Log.d(TAG, "文件无效，已删除")
                }
                isValid
            } ?: run {
                Log.e(TAG, "audioFile 为空")
                false
            }

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
    }
}