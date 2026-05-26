package com.example.countdayleave.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.countdayleave.R
import com.example.countdayleave.data.CountdownDataStore
import com.example.countdayleave.model.CountdownConfig
import com.example.countdayleave.notification.DailyNotificationReceiver
import com.example.countdayleave.ui.theme.AppTheme
import com.example.countdayleave.widget.CountdownWidgetProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    val dataStore = remember { CountdownDataStore(context) }
    var events by remember { mutableStateOf<List<CountdownConfig>>(emptyList()) }
    var jsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    // Load data
    fun refreshData() {
        coroutineScope.launch {
            val list = dataStore.eventsFlow.first()
            events = list
            // Sinh JSON thô để hiển thị
            val arr = JSONArray()
            list.forEach { config ->
                arr.put(org.json.JSONObject().apply {
                    put("id", config.id)
                    put("milestoneName", config.milestoneName)
                    put("targetEpochMillis", config.targetEpochMillis)
                    put("notifyEnabled", config.notifyEnabled)
                })
            }
            jsonText = arr.toString(2)
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel 🛠️", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Quay lại",
                            tint = AppTheme.colors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundDeep
                )
            )
        },
        containerColor = AppTheme.colors.backgroundDeep
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ---- SECTION 1: SYSTEM UTILITIES ----
            item {
                Text(
                    "Công cụ hệ thống",
                    color = AppTheme.colors.accentPurpleLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Test Notification
                        Button(
                            onClick = {
                                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                val notification = NotificationCompat.Builder(context, DailyNotificationReceiver.CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle("Thông báo kiểm thử ⚙️")
                                    .setContentText("Đây là thông báo được gửi trực tiếp từ màn hình Admin.")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true)
                                    .build()
                                nm.notify(999, notification)
                                Toast.makeText(context, "Đã gửi thông báo test!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gửi thông báo test 🔔", color = Color.White)
                        }

                        // Force Update Widgets
                        Button(
                            onClick = {
                                CountdownWidgetProvider.updateAllWidgets(context)
                                Toast.makeText(context, "Đã cập nhật toàn bộ widget!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cập nhật lại Widget 🔄", color = Color.White)
                        }

                        // Reset Application State
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    dataStore.clearAll()
                                    refreshData()
                                    CountdownWidgetProvider.updateAllWidgets(context)
                                    Toast.makeText(context, "Đã xóa sạch database!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Xóa toàn bộ dữ liệu ⚠️", color = Color.White)
                        }
                    }
                }
            }

            // ---- SECTION 2: EVENT DATABASE ----
            item {
                Text(
                    "Danh sách sự kiện (${events.size})",
                    color = AppTheme.colors.accentPurpleLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (events.isEmpty()) {
                item {
                    Text(
                        "Không có sự kiện nào trong database.",
                        color = AppTheme.colors.textMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(events) { event ->
                    val dateFormatted = remember(event.targetEpochMillis) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(event.targetEpochMillis))
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${event.milestoneName}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                            Text("ID: ${event.id}", fontSize = 11.sp, color = AppTheme.colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Đích: $dateFormatted", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                            Text("Báo thức: ${if (event.notifyEnabled) "BẬT" else "TẮT"}", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                            
                            Spacer(Modifier.height(12.dp))

                            // Time Traveler & Actions Flow
                            Text("Cấu hình thời gian kiểm thử:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.accentPurpleLight)
                            Spacer(Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add 1 day
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val updated = event.copy(targetEpochMillis = event.targetEpochMillis + 24 * 60 * 60 * 1000)
                                            dataStore.saveEvent(updated)
                                            CountdownWidgetProvider.updateAllWidgetsForEvent(context, event.id)
                                            refreshData()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+1 Ngày", fontSize = 11.sp, color = AppTheme.colors.textPrimary)
                                }

                                // Subtract 1 day
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val updated = event.copy(targetEpochMillis = event.targetEpochMillis - 24 * 60 * 60 * 1000)
                                            dataStore.saveEvent(updated)
                                            CountdownWidgetProvider.updateAllWidgetsForEvent(context, event.id)
                                            refreshData()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("-1 Ngày", fontSize = 11.sp, color = AppTheme.colors.textPrimary)
                                }

                                // Trigger celebration (Set Target to 5 seconds ago)
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val updated = event.copy(targetEpochMillis = System.currentTimeMillis() - 5000)
                                            dataStore.saveEvent(updated)
                                            CountdownWidgetProvider.updateAllWidgetsForEvent(context, event.id)
                                            refreshData()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple.copy(alpha = 0.8f)),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("Đặt đã qua", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dataStore.deleteEvent(event.id)
                                            CountdownWidgetProvider.updateAllWidgetsForEvent(context, event.id)
                                            refreshData()
                                        }
                                    }
                                ) {
                                    Text("XÓA SỰ KIỆN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ---- SECTION 3: EXPORT / IMPORT JSON ----
            item {
                Text(
                    "Export / Import dữ liệu JSON",
                    color = AppTheme.colors.accentPurpleLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = jsonText,
                            fontSize = 10.sp,
                            color = AppTheme.colors.textSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(jsonText))
                                    Toast.makeText(context, "Đã sao chép database JSON!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Copy JSON", color = AppTheme.colors.textPrimary)
                            }

                            Button(
                                onClick = {
                                    importText = ""
                                    showImportDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Import JSON", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = AppTheme.colors.surfaceCard,
            title = { Text("Import Database JSON", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nhập chuỗi JSON hợp lệ để ghi đè dữ liệu cũ:", color = AppTheme.colors.textSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("[{...}]", color = AppTheme.colors.textMuted) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val arr = JSONArray(importText)
                                // Clear old events first
                                dataStore.clearAll()
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    val config = CountdownConfig(
                                        id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                                        milestoneName = obj.getString("milestoneName"),
                                        targetEpochMillis = obj.getLong("targetEpochMillis"),
                                        notifyTimes = emptyList(),
                                        notifyEnabled = obj.optBoolean("notifyEnabled", true)
                                    )
                                    dataStore.saveEvent(config)
                                }
                                showImportDialog = false
                                refreshData()
                                CountdownWidgetProvider.updateAllWidgets(context)
                                Toast.makeText(context, "Import thành công!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "JSON không hợp lệ: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Ghi đè", color = AppTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Hủy", color = AppTheme.colors.textMuted)
                }
            }
        )
    }
}
