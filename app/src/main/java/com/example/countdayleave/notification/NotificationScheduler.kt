package com.example.countdayleave.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.countdayleave.model.CountdownConfig
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    companion object {
        const val REQUEST_CODE = 1001
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Đặt một alarm one-shot (setExactAndAllowWhileIdle) vào giờ gần nhất trong [config.notifyTimes].
     * Khi alarm kích hoạt, [DailyNotificationReceiver] sẽ gửi thông báo rồi tự gọi [schedule] lại
     * để đặt alarm cho ngày hôm sau → mô phỏng alarm lặp lại hằng ngày.
     * Sử dụng setExactAndAllowWhileIdle để alarm hoạt động ngay cả khi máy ở chế độ Doze.
     */
    fun schedule(config: CountdownConfig) {
        val intent = buildIntent()
        // Huỷ alarm cũ nếu có
        alarmManager.cancel(intent)

        if (config.notifyTimes.isEmpty()) return

        val nextTimes = config.notifyTimes.map { 
            nextTriggerTime(it.hour, it.minute)
        }
        val triggerTime = nextTimes.minOrNull() ?: return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intent
        )
    }

    /** Huỷ alarm đang đặt. */
    fun cancel() {
        alarmManager.cancel(buildIntent())
    }

    private fun buildIntent(): PendingIntent {
        val intent = Intent(context, DailyNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerTime(
        hour: Int,
        minute: Int,
        forceNextDay: Boolean = false
    ): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Nếu giờ đã qua trong ngày hôm nay (hoặc force next day), dời sang ngày mai
        if (forceNextDay || cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
