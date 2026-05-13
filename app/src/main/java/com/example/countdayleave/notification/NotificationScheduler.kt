package com.example.countdayleave.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.countdayleave.model.CountdownConfig
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        // Base request code — mỗi event dùng hashCode của eventId làm request code riêng
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Đặt alarm cho một sự kiện cụ thể.
     * Mỗi sự kiện có PendingIntent riêng biệt theo eventId.
     */
    fun schedule(config: CountdownConfig) {
        val intent = buildIntent(config.id)
        alarmManager.cancel(intent)

        if (!config.notifyEnabled || config.notifyTimes.isEmpty()) return

        val triggerTime = config.notifyTimes
            .map { nextTriggerTime(it.hour, it.minute) }
            .minOrNull() ?: return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intent
        )
    }

    /** Huỷ alarm của một sự kiện cụ thể theo eventId. */
    fun cancel(eventId: String) {
        alarmManager.cancel(buildIntent(eventId))
    }

    /** Huỷ tất cả alarm của một danh sách sự kiện. */
    fun cancelAll(eventIds: List<String>) {
        eventIds.forEach { cancel(it) }
    }

    private fun buildIntent(eventId: String): PendingIntent {
        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        // Request code dựa trên hashCode của eventId để mỗi sự kiện có PendingIntent độc lập
        val requestCode = eventId.hashCode() and 0x7FFFFFFF
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerTime(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
