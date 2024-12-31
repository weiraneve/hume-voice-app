package com.hume.voice.voice

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.getViewModel

@Composable
fun VoicePage(
    voiceViewModel: VoiceViewModel = getViewModel(),
) {
    Text("Voice page")
}