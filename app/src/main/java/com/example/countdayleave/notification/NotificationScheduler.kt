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
     * Đặt alarm lặp lại hằng ngày vào giờ đã thiết lập.
     * Sử dụng setExactAndAllowWhileIdle để alarm hoạt động ngay cả khi máy ở chế độ Doze.
     * Sau mỗi lần nhận alarm, Receiver sẽ tự schedule lần tiếp theo.
     */
    fun schedule(config: CountdownConfig) {
        val intent = buildIntent()
        // Huỷ alarm cũ nếu có
        alarmManager.cancel(intent)

        val triggerTime = nextTriggerTime(config.notifyHour, config.notifyMinute)
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

    /** Schedule lần tiếp theo (gọi từ Receiver sau khi đã xử lý). */
    fun scheduleNext(hour: Int, minute: Int) {
        val intent = buildIntent()
        val triggerTime = nextTriggerTime(hour, minute, forceNextDay = true)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intent
        )
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
