package com.example.countdayleave.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.countdayleave.MainActivity
import com.example.countdayleave.R
import com.example.countdayleave.data.CountdownDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "countdown_channel"
        const val NOTIFICATION_ID = 1
        // Intent action để mở CelebrationScreen
        const val ACTION_OPEN_CELEBRATION = "com.example.countdayleave.OPEN_CELEBRATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Sử dụng goAsync để chạy coroutine từ BroadcastReceiver
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val dataStore = CountdownDataStore(context)
                val config = dataStore.configFlow.first()

                if (config == null || !config.notifyEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val now = System.currentTimeMillis()
                val diff = config.targetEpochMillis - now

                val (title, body, tapAction) = if (diff > 0) {
                    val totalSeconds = diff / 1000
                    val days    = totalSeconds / 86400
                    val hours   = (totalSeconds % 86400) / 3600
                    val minutes = (totalSeconds % 3600) / 60

                    val timeText = buildString {
                        if (days > 0) append("${days} ngày ")
                        if (hours > 0) append("${hours} giờ ")
                        if (days == 0L && minutes > 0) append("${minutes} phút ")
                    }.trim()

                    Triple(
                        "⏳ ${config.milestoneName}",
                        "Còn $timeText nữa thôi!",
                        "countdown"
                    )
                } else {
                    Triple(
                        "🎉 Chúc mừng!",
                        "Đã đến ngày ${config.milestoneName} rồi!",
                        "celebration"
                    )
                }

                sendNotification(context, title, body, tapAction)

                // Tính toán và schedule alarm cho thời điểm kế tiếp
                val scheduler = NotificationScheduler(context)
                scheduler.schedule(config)

            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(
        context: Context,
        title: String,
        body: String,
        tapAction: String
    ) {
        // Tap notification → mở MainActivity với deep-link action
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            action = tapAction
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
