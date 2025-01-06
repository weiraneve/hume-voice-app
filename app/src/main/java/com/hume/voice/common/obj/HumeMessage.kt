package com.hume.voice.common.obj

data class HumeMessage(
    val type: String,
    val message: Message,
) {
    data class Message(
        val role: String,
        val content: String
    )
}