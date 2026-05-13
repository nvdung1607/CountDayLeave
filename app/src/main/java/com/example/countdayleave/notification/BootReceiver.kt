package com.example.countdayleave.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdayleave.data.CountdownDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedule tất cả alarm sau khi thiết bị khởi động lại.
 * AlarmManager bị xóa sau reboot, receiver này phục hồi lại cho mọi sự kiện.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val events = CountdownDataStore(context).eventsFlow.first()
                val scheduler = NotificationScheduler(context)
                events.filter { it.notifyEnabled }.forEach { config ->
                    scheduler.schedule(config)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
