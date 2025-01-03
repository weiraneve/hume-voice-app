package com.hume.voice.voice

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoicePage(
    voiceViewModel: VoiceViewModel = getViewModel(),
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val isRecording by voiceViewModel.isRecording.collectAsState()
    val messages by voiceViewModel.messages.collectAsState()

    LaunchedEffect(Unit) {
        voiceViewModel.setCacheDir(context.cacheDir)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    when (permissionState.status) {
                        PermissionStatus.Granted -> {
                            if (isRecording) {
                                val isSuccess = voiceViewModel.stopRecording()
                                if (!isSuccess) {
                                    Toast.makeText(context, "录音停止失败，请重试", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                voiceViewModel.startRecording()
                            }
                        }

                        is PermissionStatus.Denied -> {
                            permissionState.launchPermissionRequest()
                        }
                    }
                }
            ) {
                Text(if (isRecording) "停止录音" else "开始录音")
            }

            if (messages.isNotEmpty()) {
                Text(
                    text = messages,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}