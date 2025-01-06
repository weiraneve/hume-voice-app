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

                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }
                }
                loadUrl(currentUrl.value)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}