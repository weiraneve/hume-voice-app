package com.hume.voice.modules

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.androidx.compose.getViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HumePage(
    humeViewModel: HumeViewModel = getViewModel(),
) {
    val currentUrl by humeViewModel.webUrl.collectAsState()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
            }
        },
        update = { webView ->
            webView.loadUrl(currentUrl)
        }
    )
}