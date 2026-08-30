package com.nvdung1607.countdayleave.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nvdung1607.countdayleave.MainActivity
import com.nvdung1607.countdayleave.R
import com.nvdung1607.countdayleave.data.CountdownDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "countdown_channel"
        const val EXTRA_EVENT_ID = "event_id"
        const val ACTION_OPEN_CELEBRATION = "com.nvdung1607.countdayleave.OPEN_CELEBRATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val eventId = intent.getStringExtra(EXTRA_EVENT_ID)

                val dataStore = CountdownDataStore(context)
                val allEvents = dataStore.eventsFlow.first()

                // Tìm đúng event cần thông báo
                val config = if (eventId != null) {
                    allEvents.find { it.id == eventId }
                } else {
                    // Fallback: lấy event đầu tiên (backward compat)
                    allEvents.firstOrNull()
                }

                if (config == null || !config.notifyEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val now = System.currentTimeMillis()
                
                val title: String
                val body: String
                val tapAction: String
                
                if (config.isCountUp) {
                    val countUpDiff = now - config.targetEpochMillis
                    val diffToUse = if (countUpDiff > 0) countUpDiff else 0L
                    val totalSeconds = diffToUse / 1000
                    val days    = totalSeconds / 86400
                    val hours   = (totalSeconds % 86400) / 3600
                    val minutes = (totalSeconds % 3600) / 60

                    val timeText = buildString {
                        if (days > 0) append("${days} ngày ")
                        if (hours > 0) append("${hours} giờ ")
                        if (days == 0L && minutes > 0) append("${minutes} phút ")
                    }.trim()

                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    val quoteIndex = prefs.getInt("quote_index_$dateKey", com.nvdung1607.countdayleave.data.QuoteRepository.getQuoteOfTheDayIndex(context))
                    val quote = com.nvdung1607.countdayleave.data.QuoteRepository.getQuote(context, quoteIndex)
                    
                    title = "${config.milestoneName}: Đã qua $timeText"
                    body = "💡 \"$quote\""
                    tapAction = "countdown"
                } else {
                    val diff = config.targetEpochMillis - now
                    if (diff > 0) {
                        val totalSeconds = diff / 1000
                        val days    = totalSeconds / 86400
                        val hours   = (totalSeconds % 86400) / 3600
                        val minutes = (totalSeconds % 3600) / 60

                        val timeText = buildString {
                            if (days > 0) append("${days} ngày ")
                            if (hours > 0) append("${hours} giờ ")
                            if (days == 0L && minutes > 0) append("${minutes} phút ")
                        }.trim()

                        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                        val quoteIndex = prefs.getInt("quote_index_$dateKey", com.nvdung1607.countdayleave.data.QuoteRepository.getQuoteOfTheDayIndex(context))
                        val quote = com.nvdung1607.countdayleave.data.QuoteRepository.getQuote(context, quoteIndex)
                        
                        title = "${config.milestoneName}: Còn $timeText"
                        body = "💡 \"$quote\""
                        tapAction = "countdown"
                    } else {
                        title = "${config.milestoneName}: Đã đến hạn rồi! 🎉"
                        body = "Chúc mừng bạn đã hoàn thành mục tiêu!"
                        tapAction = "celebration"
                    }
                }

                // Cập nhật lại Widget khi chu kỳ alarm chạy
                com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgets(context)

                // Notification ID dựa theo eventId để mỗi sự kiện có notification riêng
                val notificationId = config.id.hashCode() and 0x7FFFFFFF

                sendNotification(context, title, body, tapAction, config.id, notificationId)

                // Re-schedule alarm cho lần tiếp theo
                if (config.isCountUp || config.targetEpochMillis - now > 0) {
                    val scheduler = NotificationScheduler(context)
                    scheduler.schedule(config)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(
        context: Context,
        title: String,
        body: String,
        tapAction: String,
        eventId: String,
        notificationId: Int
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            action = tapAction
            putExtra(NotificationScheduler.EXTRA_EVENT_ID, eventId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
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
        nm.notify(notificationId, notification)
    }
}

