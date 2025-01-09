package com.hume.voice.modules

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.hume.voice.common.network.WebAppInterface
import com.hume.voice.common.obj.HumeMessage
import org.koin.androidx.compose.getViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HumePage(
    humeViewModel: HumeViewModel = getViewModel(),
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
    }

    LaunchedEffect(Unit) {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                )
            )
        } else {
            permissionsGranted = true
        }
    }

    if (permissionsGranted) {
        val currentUrl = humeViewModel.webUrl.collectAsState()
        val messages = humeViewModel.messages.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            ) {
                items(messages.value) { message ->
                    MessageItem(message)
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                        }

                        addJavascriptInterface(WebAppInterface(humeViewModel), "Android")

                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest) {
                                val resources = arrayOf(
                                    PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                                    PermissionRequest.RESOURCE_VIDEO_CAPTURE
                                )
                                request.grant(resources)
                            }
                        }

                        // 注入 JavaScript 代码来监听 WebSocket 消息
                        evaluateJavascript(
                            """
                        console.log('Setting up message listener');
                        window.addEventListener('message', function(event) {
                            console.log('Message received:', event.data);
                            if (event.data && typeof event.data === 'object') {
                                console.log('Sending to Android:', JSON.stringify(event.data));
                                Android.onMessageReceived(JSON.stringify(event.data));
                            }
                        });
                        console.log('Message listener setup complete');
                    """.trimIndent(), null
                        )

                        loadUrl(currentUrl.value)
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageItem(message: HumeMessage) {
    val backgroundColor = when (message.type) {
        "assistant_message" -> Color.LightGray
        "user_message" -> Color.Blue.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = message.message.content,
            modifier = Modifier.padding(16.dp)
        )
    }
}