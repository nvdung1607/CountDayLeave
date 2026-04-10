package com.example.countdayleave.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdayleave.data.CountdownDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedule daily alarm sau khi thiết bị khởi động lại.
 * AlarmManager bị xóa sau reboot, receiver này phục hồi lại.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = CountdownDataStore(context).configFlow.first()
                if (config != null && config.notifyEnabled) {
                    NotificationScheduler(context).schedule(config)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
