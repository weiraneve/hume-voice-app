package com.hume.voice.modules

import android.annotation.SuppressLint
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hume.voice.common.network.WebAppInterface
import org.koin.androidx.compose.getViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HumePage(
    humeViewModel: HumeViewModel = getViewModel(),
) {
    val currentUrl = humeViewModel.webUrl.collectAsState()

    AndroidView(
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
                        request.grant(request.resources)
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
        },
        modifier = Modifier.fillMaxSize()
    )
}