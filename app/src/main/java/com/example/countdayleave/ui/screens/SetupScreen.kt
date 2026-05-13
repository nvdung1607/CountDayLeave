package com.example.countdayleave.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdayleave.ui.components.AppDatePickerDialog
import com.example.countdayleave.ui.components.AppTimePickerDialog
import com.example.countdayleave.ui.theme.*
import com.example.countdayleave.model.NotifyTime
import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_TARGET_HOUR = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    initialMilestoneName: String = "",
    initialTargetMillis: Long? = null,
    initialNotifyTimes: List<NotifyTime> = listOf(NotifyTime(8, 0)),
    initialNotifyEnabled: Boolean = true,
    isEditing: Boolean = false,
    onSave: (
        milestoneName: String,
        targetEpochMillis: Long,
        notifyTimes: List<NotifyTime>,
        notifyEnabled: Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    // ---- State ----
    var milestoneName by remember { mutableStateOf(initialMilestoneName) }
    var targetDateMillis by remember { mutableStateOf(initialTargetMillis) }
    var targetHour by remember { mutableIntStateOf(
        if (initialTargetMillis != null) {
            Calendar.getInstance().apply { timeInMillis = initialTargetMillis }.get(Calendar.HOUR_OF_DAY)
        } else DEFAULT_TARGET_HOUR
    ) }
    var targetMinute by remember { mutableIntStateOf(
        if (initialTargetMillis != null) {
            Calendar.getInstance().apply { timeInMillis = initialTargetMillis }.get(Calendar.MINUTE)
        } else 0
    ) }
    var notifyTimes by remember { mutableStateOf(initialNotifyTimes) }
    var notifyEnabled by remember { mutableStateOf(initialNotifyEnabled) }

    // ---- Dialog visibility ----
    var showDatePicker by remember { mutableStateOf(false) }
    var showTargetTimePicker by remember { mutableStateOf(false) }
    var showNotifyTimePicker by remember { mutableStateOf(false) }
    var editTimeIndex by remember { mutableStateOf<Int?>(null) }

    // ---- Validation ----
    val isValid = milestoneName.isNotBlank() && targetDateMillis != null && (!notifyEnabled || notifyTimes.isNotEmpty())

    // ---- Helper: combine date + time → epoch millis ----
    fun buildTargetEpoch(): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = targetDateMillis!!
        cal.set(Calendar.HOUR_OF_DAY, targetHour)
        cal.set(Calendar.MINUTE, targetMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ---- Dialogs ----
    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = targetDateMillis,
            onDateSelected = { targetDateMillis = it },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTargetTimePicker) {
        AppTimePickerDialog(
            initialHour = targetHour,
            initialMinute = targetMinute,
            title = "Giờ đến đích",
            onTimeSelected = { h, m -> targetHour = h; targetMinute = m },
            onDismiss = { showTargetTimePicker = false }
        )
    }
    if (showNotifyTimePicker) {
        val initHour = editTimeIndex?.let { notifyTimes.getOrNull(it)?.hour } ?: 8
        val initMinute = editTimeIndex?.let { notifyTimes.getOrNull(it)?.minute } ?: 0

        AppTimePickerDialog(
            initialHour = initHour,
            initialMinute = initMinute,
            title = if (editTimeIndex == null) "Thêm giờ nhắc mới" else "Chỉnh sửa giờ",
            onTimeSelected = { h, m -> 
                val newTime = NotifyTime(h, m)
                if (editTimeIndex == null) {
                    if (!notifyTimes.contains(newTime)) {
                        notifyTimes = (notifyTimes + newTime).sortedWith(compareBy({ it.hour }, { it.minute }))
                    }
                } else {
                    val newList = notifyTimes.toMutableList()
                    newList[editTimeIndex!!] = newTime
                    notifyTimes = newList.distinct().sortedWith(compareBy({ it.hour }, { it.minute }))
                }
            },
            onDismiss = { showNotifyTimePicker = false }
        )
    }

    // ---- UI ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Quay lại",
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Header
            SetupHeader(isEditing = isEditing)

            Spacer(Modifier.height(32.dp))

            // Tên mốc thời gian
            SectionLabel(text = "TÊN MỐC THỜI GIAN")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = milestoneName,
                onValueChange = { milestoneName = it },
                placeholder = {
                    Text("Ví dụ: Ngày nghỉ việc", color = AppTheme.colors.textMuted)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.accentPurple,
                    unfocusedBorderColor = AppTheme.colors.textMuted,
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    cursorColor = AppTheme.colors.accentPurple,
                    focusedContainerColor = AppTheme.colors.surfaceCard,
                    unfocusedContainerColor = AppTheme.colors.surfaceCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Thời gian đến đích
            SectionLabel(text = "THỜI GIAN ĐẾN ĐÍCH")
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chọn ngày
                PickerCard(
                    icon = Icons.Rounded.CalendarMonth,
                    label = "Ngày",
                    value = targetDateMillis?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Chưa chọn",
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true }
                )
                // Chọn giờ
                PickerCard(
                    icon = Icons.Rounded.Schedule,
                    label = "Giờ",
                    value = String.format("%02d:%02d", targetHour, targetMinute),
                    modifier = Modifier.weight(1f),
                    onClick = { showTargetTimePicker = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Thông báo hằng ngày
            SectionLabel(text = "THÔNG BÁO HẰNG NGÀY")
            Spacer(Modifier.height(8.dp))

            // Toggle + time pick
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surfaceCard)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = AppTheme.colors.accentPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Nhận thông báo hằng ngày",
                            color = AppTheme.colors.textPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notifyEnabled,
                            onCheckedChange = { notifyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppTheme.colors.textPrimary,
                                checkedTrackColor = AppTheme.colors.accentPurple,
                                uncheckedThumbColor = AppTheme.colors.textMuted,
                                uncheckedTrackColor = AppTheme.colors.surfaceElevated
                            )
                        )
                    }

                    AnimatedVisibility(visible = notifyEnabled) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = AppTheme.colors.surfaceElevated)
                            Spacer(Modifier.height(12.dp))
                            
                            notifyTimes.forEachIndexed { index, time ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppTheme.colors.surfaceElevated)
                                        .clickable { 
                                            editTimeIndex = index
                                            showNotifyTimePicker = true 
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        tint = AppTheme.colors.accentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Thông báo lúc",
                                        color = AppTheme.colors.textSecondary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        String.format("%02d:%02d", time.hour, time.minute),
                                        color = AppTheme.colors.accentBlue,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            notifyTimes = notifyTimes.toMutableList().apply { removeAt(index) }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Xóa",
                                            tint = AppTheme.colors.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            
                            // Nút thêm giờ
                            TextButton(
                                onClick = {
                                    editTimeIndex = null
                                    showNotifyTimePicker = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.accentPurple)
                                Spacer(Modifier.width(8.dp))
                                Text("Thêm giờ thông báo", color = AppTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Nút lưu
            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            milestoneName.trim(),
                            buildTargetEpoch(),
                            notifyTimes,
                            notifyEnabled
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = AppTheme.colors.textPrimary,
                    disabledContainerColor = AppTheme.colors.surfaceElevated,
                    disabledContentColor = AppTheme.colors.textMuted
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isValid)
                                Brush.horizontalGradient(listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd))
                            else
                                Brush.horizontalGradient(listOf(AppTheme.colors.surfaceElevated, AppTheme.colors.surfaceElevated)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEditing) "💾  Lưu thay đổi" else "🚀  Bắt đầu đếm ngược",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ============================================================
// Sub-composables
// ============================================================

@Composable
private fun SetupHeader(isEditing: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icon circle
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(80.dp)
                .background(
                    Brush.radialGradient(listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd.copy(alpha = 0.3f))),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isEditing) "✏️" else "⏳", fontSize = 36.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isEditing) "Chỉnh sửa mốc thời gian" else "Thiết lập mốc thời gian",
            color = AppTheme.colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Đặt tên và mốc thời gian bạn muốn đếm ngược",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PickerCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surfaceCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.accentPurple,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(label, color = AppTheme.colors.textMuted, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
