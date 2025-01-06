package com.hume.voice.common.network

import android.content.Context
import android.webkit.JavascriptInterface

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun onCallStart() {
        // 处理从 Web 端发起的通话请求
    }

    @JavascriptInterface
    fun onCallEnd() {
        // 处理从 Web 端结束的通话
    }
}