package com.example.countdayleave.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdayleave.data.CountdownDataStore
import com.example.countdayleave.model.CountdownConfig
import com.example.countdayleave.ui.theme.AppTheme
import com.example.countdayleave.ui.theme.CountDayLeaveTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Phản hồi mặc định nếu người dùng hủy/nhấn back
        setResult(RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            CountDayLeaveTheme {
                WidgetConfigureScreen(
                    onEventSelected = { eventId ->
                        saveWidgetConfig(this, appWidgetId, eventId)
                        
                        // Cập nhật widget ngay lập tức
                        val appWidgetManager = AppWidgetManager.getInstance(this)
                        CountdownWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

                        // Trả kết quả thành công và thoát
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "widget_prefs"
        private const val PREF_KEY_PREFIX = "widget_"

        fun saveWidgetConfig(context: Context, appWidgetId: Int, eventId: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("$PREF_KEY_PREFIX$appWidgetId", eventId)
                .apply()
        }

        fun getWidgetConfig(context: Context, appWidgetId: Int): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("$PREF_KEY_PREFIX$appWidgetId", null)
        }

        fun deleteWidgetConfig(context: Context, appWidgetId: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove("$PREF_KEY_PREFIX$appWidgetId")
                .apply()
        }
    }
}

@Composable
fun WidgetConfigureScreen(
    onEventSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<CountdownConfig>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val store = CountdownDataStore(context)
            events = store.eventsFlow.first()
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Chọn sự kiện cho Widget",
                color = AppTheme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppTheme.colors.accentPurple)
                }
            } else if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Không tìm thấy sự kiện nào.\nVui lòng tạo sự kiện trong ứng dụng trước.",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentPurple
                        )
                    ) {
                        Text("Quay lại")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events) { event ->
                        val targetDate = remember(event.targetEpochMillis) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(event.targetEpochMillis))
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventSelected(event.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.surfaceCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                AppTheme.colors.accentPurple.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = event.milestoneName,
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Ngày đích: $targetDate",
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
