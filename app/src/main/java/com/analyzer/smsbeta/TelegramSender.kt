package com.analyzer.smsbeta

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

object TelegramSender {
    private const val BOT_TOKEN = "YOUR_BOT_TOKEN"
    private const val CHAT_ID = "YOUR_CHAT_ID"

    fun sendToTelegram(message: String) {
        Thread {
            try {
                val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?" +
                        "chat_id=$CHAT_ID&text=${URLEncoder.encode(message, "UTF-8")}"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}