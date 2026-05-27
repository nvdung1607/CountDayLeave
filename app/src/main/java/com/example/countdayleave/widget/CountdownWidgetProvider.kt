package com.example.countdayleave.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.countdayleave.MainActivity
import com.example.countdayleave.R
import com.example.countdayleave.data.CountdownDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == "android.intent.action.TIME_SET" ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_BOOT_COMPLETED
        ) {
            updateAllWidgets(context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetConfigureActivity.deleteWidgetConfig(context, appWidgetId)
        }
    }

    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            coroutineScope.launch {
                val views = RemoteViews(context.packageName, R.layout.countdown_widget)
                val eventId = WidgetConfigureActivity.getWidgetConfig(context, appWidgetId)

                 if (eventId == null) {
                    views.setTextViewText(R.id.widget_title, "Chưa cấu hình")
                    views.setTextViewText(R.id.widget_days, "--")
                    views.setTextViewText(R.id.widget_days_label, "NHẤP ĐỂ THIẾT LẬP")
                    views.setTextViewText(R.id.widget_target_date, "")
                    views.setTextViewText(R.id.widget_quote, "")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return@launch
                }

                // Load event từ DataStore
                val dataStore = CountdownDataStore(context)
                val events = dataStore.eventsFlow.first()
                val event = events.find { it.id == eventId }

                if (event == null) {
                    views.setTextViewText(R.id.widget_title, "Sự kiện đã bị xóa")
                    views.setTextViewText(R.id.widget_days, "N/A")
                    views.setTextViewText(R.id.widget_days_label, "XÓA WIDGET NÀY")
                    views.setTextViewText(R.id.widget_target_date, "")
                    views.setTextViewText(R.id.widget_quote, "")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return@launch
                }

                // Tính toán số ngày còn lại
                val now = System.currentTimeMillis()
                val diff = event.targetEpochMillis - now

                val daysLeft = if (diff > 0) {
                    // Làm tròn lên số ngày còn lại
                    (diff + (1000 * 60 * 60 * 24 - 1)) / (1000 * 60 * 60 * 24)
                } else {
                    0L
                }

                val targetDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(event.targetEpochMillis))

                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val quoteIndex = prefs.getInt("quote_index_$dateKey", com.example.countdayleave.data.QuoteRepository.getQuoteOfTheDayIndex(context))
                val quote = com.example.countdayleave.data.QuoteRepository.getQuote(context, quoteIndex)

                // Cập nhật text hiển thị
                views.setTextViewText(R.id.widget_title, event.milestoneName)
                if (daysLeft > 0) {
                    views.setTextViewText(R.id.widget_days, daysLeft.toString())
                    views.setTextViewText(R.id.widget_days_label, "NGÀY CÒN LẠI")
                } else {
                    views.setTextViewText(R.id.widget_days, "🎉")
                    views.setTextViewText(R.id.widget_days_label, "ĐÃ ĐẾN NGÀY")
                }
                views.setTextViewText(R.id.widget_target_date, "🎯 $targetDateStr")
                views.setTextViewText(R.id.widget_quote, "“$quote”")

                // Cấu hình Intent khi click vào widget: Mở trực tiếp màn đếm ngược của sự kiện này
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = "countdown"
                    putExtra(com.example.countdayleave.notification.NotificationScheduler.EXTRA_EVENT_ID, event.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                // Cập nhật widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        /**
         * Trigger cập nhật cho tất cả widget của một sự kiện cụ thể.
         */
        fun updateAllWidgetsForEvent(context: Context, eventId: String) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CountdownWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                val configuredEventId = WidgetConfigureActivity.getWidgetConfig(context, id)
                if (configuredEventId == eventId) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
        
        /**
         * Trigger cập nhật cho tất cả widget.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CountdownWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
