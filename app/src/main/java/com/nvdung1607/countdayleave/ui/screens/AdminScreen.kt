package com.nvdung1607.countdayleave.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import com.nvdung1607.countdayleave.data.CountdownDataStore
import com.nvdung1607.countdayleave.data.QuoteRepository
import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.notification.DailyNotificationReceiver
import com.nvdung1607.countdayleave.ui.theme.AppTheme
import com.nvdung1607.countdayleave.widget.CountdownWidgetProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onNavigateToCelebration: (eventId: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    val dataStore = remember { CountdownDataStore(context) }
    var events by remember { mutableStateOf<List<CountdownConfig>>(emptyList()) }
    var quotes by remember { mutableStateOf<List<String>>(emptyList()) }
    var jsonText by remember { mutableStateOf("") }

    // Dialog state for Event Add/Edit
    var showEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CountdownConfig?>(null) }
    var eventName by remember { mutableStateOf("") }
    var eventEpochMillis by remember { mutableStateOf(0L) }

    // Dialog state for Quote Add/Edit
    var showQuoteDialog by remember { mutableStateOf(false) }
    var editingQuoteIndex by remember { mutableStateOf<Int?>(null) }
    var quoteText by remember { mutableStateOf("") }

    // JSON Import state
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    // Load data
    fun refreshData() {
        coroutineScope.launch {
            val list = dataStore.eventsFlow.first()
            events = list
            quotes = QuoteRepository.getAllQuotes(context)

            // Generate raw JSON representation
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
                        // Test Real Notification
                        Button(
                            onClick = {
                                val firstEvent = events.firstOrNull()
                                if (firstEvent != null) {
                                    val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
                                        putExtra(DailyNotificationReceiver.EXTRA_EVENT_ID, firstEvent.id)
                                    }
                                    context.sendBroadcast(intent)
                                    Toast.makeText(context, "Kích hoạt thông báo thực tế thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Vui lòng tạo ít nhất 1 sự kiện để test thông báo thật!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gửi thông báo test thật 🔔", color = Color.White)
                        }

                        // Test Celebration Screen directly
                        Button(
                            onClick = {
                                val targetEvent = events.firstOrNull()
                                if (targetEvent != null) {
                                    onNavigateToCelebration(targetEvent.id)
                                } else {
                                    // Create a Mock event if no events exist
                                    coroutineScope.launch {
                                        val mockEvent = CountdownConfig(
                                            id = UUID.randomUUID().toString(),
                                            milestoneName = "Sự kiện kiểm thử chúc mừng 🏆",
                                            targetEpochMillis = System.currentTimeMillis() - 5000,
                                            notifyTimes = emptyList(),
                                            notifyEnabled = false
                                        )
                                        dataStore.saveEvent(mockEvent)
                                        refreshData()
                                        Toast.makeText(context, "Đã tạo sự kiện kiểm thử. Đang mở...", Toast.LENGTH_SHORT).show()
                                        onNavigateToCelebration(mockEvent.id)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.gradientStart),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Màn hình hoàn thành 🏆", color = Color.White)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Danh sách sự kiện (${events.size})",
                        color = AppTheme.colors.accentPurpleLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            editingEvent = null
                            eventName = ""
                            eventEpochMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                            showEventDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Thêm", fontSize = 12.sp)
                    }
                }
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

                            // Time Traveler Controls
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

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                            ) {
                                TextButton(
                                    onClick = {
                                        editingEvent = event
                                        eventName = event.milestoneName
                                        eventEpochMillis = event.targetEpochMillis
                                        showEventDialog = true
                                    }
                                ) {
                                    Text("SỬA", color = AppTheme.colors.accentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dataStore.deleteEvent(event.id)
                                            CountdownWidgetProvider.updateAllWidgetsForEvent(context, event.id)
                                            refreshData()
                                        }
                                    }
                                ) {
                                    Text("XÓA", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ---- SECTION 3: QUOTE DATABASE ----
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Danh sách Quote (${quotes.size})",
                        color = AppTheme.colors.accentPurpleLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            editingQuoteIndex = null
                            quoteText = ""
                            showQuoteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentPurple),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Thêm", fontSize = 12.sp)
                    }
                }
            }

            itemsIndexed(quotes) { index, quote ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${index + 1}]",
                            fontSize = 11.sp,
                            color = AppTheme.colors.accentPurpleLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp)
                        )
                        
                        Text(
                            text = quote,
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        // Edit Quote button
                        IconButton(
                            onClick = {
                                editingQuoteIndex = index
                                quoteText = quote
                                showQuoteDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Sửa quote", tint = AppTheme.colors.accentBlue, modifier = Modifier.size(16.dp))
                        }

                        // Delete Quote button
                        IconButton(
                            onClick = {
                                QuoteRepository.deleteQuote(context, index)
                                refreshData()
                                CountdownWidgetProvider.updateAllWidgets(context)
                                Toast.makeText(context, "Đã xóa câu quote!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Xóa quote", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ---- SECTION 4: EXPORT / IMPORT JSON ----
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

    // ---- EVENT ADD/EDIT DIALOG ----
    if (showEventDialog) {
        val calendar = remember { Calendar.getInstance().apply { timeInMillis = eventEpochMillis } }

        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            containerColor = AppTheme.colors.surfaceCard,
            title = {
                Text(
                    if (editingEvent == null) "Thêm sự kiện" else "Sửa sự kiện",
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        label = { Text("Tên sự kiện") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(eventEpochMillis))
                    val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(eventEpochMillis))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date picker trigger
                        Button(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        calendar.set(Calendar.YEAR, y)
                                        calendar.set(Calendar.MONTH, m)
                                        calendar.set(Calendar.DAY_OF_MONTH, d)
                                        eventEpochMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(dateText, color = AppTheme.colors.textPrimary)
                        }

                        // Time picker trigger
                        Button(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        calendar.set(Calendar.HOUR_OF_DAY, h)
                                        calendar.set(Calendar.MINUTE, min)
                                        calendar.set(Calendar.SECOND, 0)
                                        eventEpochMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(timeText, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (eventName.isNotBlank()) {
                            coroutineScope.launch {
                                val config = CountdownConfig(
                                    id = editingEvent?.id ?: UUID.randomUUID().toString(),
                                    milestoneName = eventName,
                                    targetEpochMillis = eventEpochMillis,
                                    notifyTimes = editingEvent?.notifyTimes ?: emptyList(),
                                    notifyEnabled = editingEvent?.notifyEnabled ?: true
                                )
                                dataStore.saveEvent(config)
                                CountdownWidgetProvider.updateAllWidgetsForEvent(context, config.id)
                                showEventDialog = false
                                refreshData()
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng nhập tên sự kiện!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Lưu", color = AppTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) {
                    Text("Hủy", color = AppTheme.colors.textMuted)
                }
            }
        )
    }

    // ---- QUOTE ADD/EDIT DIALOG ----
    if (showQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            containerColor = AppTheme.colors.surfaceCard,
            title = {
                Text(
                    if (editingQuoteIndex == null) "Thêm Quote mới" else "Sửa Quote",
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Nội dung câu quote") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (quoteText.isNotBlank()) {
                            val idx = editingQuoteIndex
                            if (idx == null) {
                                QuoteRepository.addQuote(context, quoteText)
                            } else {
                                QuoteRepository.editQuote(context, idx, quoteText)
                            }
                            showQuoteDialog = false
                            refreshData()
                            CountdownWidgetProvider.updateAllWidgets(context)
                            Toast.makeText(context, "Lưu quote thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Lưu", color = AppTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuoteDialog = false }) {
                    Text("Hủy", color = AppTheme.colors.textMuted)
                }
            }
        )
    }

    // ---- JSON IMPORT DIALOG ----
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

