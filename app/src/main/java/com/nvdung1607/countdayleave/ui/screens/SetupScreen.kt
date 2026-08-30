package com.nvdung1607.countdayleave.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvdung1607.countdayleave.ui.components.AppDatePickerDialog
import com.nvdung1607.countdayleave.ui.components.AppTimePickerDialog
import com.nvdung1607.countdayleave.ui.theme.*
import com.nvdung1607.countdayleave.model.NotifyTime
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.os.Build
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.nvdung1607.countdayleave.ui.utils.rememberAdaptiveLayoutInfo

import androidx.activity.result.PickVisualMediaRequest
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

private const val DEFAULT_TARGET_HOUR = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    initialMilestoneName: String = "",
    initialTargetMillis: Long? = null,
    initialNotifyTimes: List<NotifyTime> = listOf(NotifyTime(8, 0)),
    initialNotifyEnabled: Boolean = true,
    initialIsCountUp: Boolean = false,
    initialBackgroundImagePath: String? = null,
    isEditing: Boolean = false,
    onSave: (
        milestoneName: String,
        targetEpochMillis: Long,
        notifyTimes: List<NotifyTime>,
        notifyEnabled: Boolean,
        isCountUp: Boolean,
        backgroundImagePath: String?
    ) -> Unit,
    onDelete: () -> Unit = {},
    onBack: () -> Unit
) {
    // ---- State ----
    var milestoneName by remember(initialMilestoneName) { mutableStateOf(initialMilestoneName) }
    var isCountUp by remember(initialIsCountUp) { mutableStateOf(initialIsCountUp) }
    var targetDateMillis by remember(initialTargetMillis) { mutableStateOf(initialTargetMillis) }
    var targetHour by remember(initialTargetMillis) { mutableIntStateOf(
        if (initialTargetMillis != null) {
            Calendar.getInstance().apply { timeInMillis = initialTargetMillis }.get(Calendar.HOUR_OF_DAY)
        } else DEFAULT_TARGET_HOUR
    ) }
    var targetMinute by remember(initialTargetMillis) { mutableIntStateOf(
        if (initialTargetMillis != null) {
            Calendar.getInstance().apply { timeInMillis = initialTargetMillis }.get(Calendar.MINUTE)
        } else 0
    ) }
    var notifyTimes by remember(initialNotifyTimes) { mutableStateOf(initialNotifyTimes) }
    var notifyEnabled by remember(initialNotifyEnabled) { mutableStateOf(initialNotifyEnabled) }
    var backgroundImagePath by remember(initialBackgroundImagePath) { mutableStateOf(initialBackgroundImagePath) }
    var isProcessingImage by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notifyEnabled = true
        } else {
            notifyEnabled = false
            Toast.makeText(context, "Ứng dụng cần quyền thông báo để gửi nhắc nhở hằng ngày!", Toast.LENGTH_LONG).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessingImage = true
                val tempId = UUID.randomUUID().toString()
                val savedPath = com.nvdung1607.countdayleave.ui.utils.ImageStorageUtils.saveImageFromUri(context, uri, tempId)
                if (savedPath != null) {
                    // Nếu đã có ảnh cũ, xóa file cũ đi
                    backgroundImagePath?.let { oldPath ->
                        if (oldPath != savedPath) {
                            com.nvdung1607.countdayleave.ui.utils.ImageStorageUtils.deleteImage(oldPath)
                        }
                    }
                    backgroundImagePath = savedPath
                } else {
                    Toast.makeText(context, "Không thể tải ảnh, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                }
                isProcessingImage = false
            }
        }
    }

    // ---- Dialog visibility ----
    var showDatePicker by remember { mutableStateOf(false) }
    var showTargetTimePicker by remember { mutableStateOf(false) }
    var showNotifyTimePicker by remember { mutableStateOf(false) }
    var editTimeIndex by remember { mutableStateOf<Int?>(null) }

    // ---- Helper: combine date + time → epoch millis ----
    fun buildTargetEpoch(): Long {
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = targetDateMillis!!
        
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        cal.set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        cal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, targetHour)
        cal.set(Calendar.MINUTE, targetMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ---- Validation ----
    val combinedEpoch = if (targetDateMillis != null) buildTargetEpoch() else null
    val isTimeInPast = combinedEpoch != null && combinedEpoch <= System.currentTimeMillis()
    val isTimeValid = if (isCountUp) isTimeInPast else !isTimeInPast
    val isValid = milestoneName.isNotBlank() && targetDateMillis != null && isTimeValid && (!notifyEnabled || notifyTimes.isNotEmpty())

    // ---- Dialogs ----
    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = targetDateMillis,
            allowPastDates = isCountUp,
            onDateSelected = { selectedDate ->
                targetDateMillis = selectedDate
                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = selectedDate }
                val localCal = Calendar.getInstance()
                val isToday = localCal.get(Calendar.YEAR) == utcCal.get(Calendar.YEAR) &&
                        localCal.get(Calendar.DAY_OF_YEAR) == utcCal.get(Calendar.DAY_OF_YEAR)
                if (isToday) {
                    val nowHour = localCal.get(Calendar.HOUR_OF_DAY)
                    val nowMinute = localCal.get(Calendar.MINUTE)
                    if (targetHour < nowHour || (targetHour == nowHour && targetMinute <= nowMinute)) {
                        val nextHourCal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
                        targetHour = nextHourCal.get(Calendar.HOUR_OF_DAY)
                        targetMinute = 0
                    }
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTargetTimePicker) {
        AppTimePickerDialog(
            initialHour = targetHour,
            initialMinute = targetMinute,
            title = if (isCountUp) "Giờ bắt đầu" else "Giờ đến đích",
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
    val adaptiveInfo = rememberAdaptiveLayoutInfo()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = adaptiveInfo.maxContentWidth)
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

            // Loại sự kiện
            SectionLabel(text = "LOẠI SỰ KIỆN")
            Spacer(Modifier.height(8.dp))
            EventTypeSegmentedControl(
                isCountUp = isCountUp,
                onTypeChanged = { isCountUp = it }
            )

            Spacer(Modifier.height(24.dp))

            // Tên mốc thời gian
            SectionLabel(text = "TÊN MỐC THỜI GIAN")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = milestoneName,
                onValueChange = { milestoneName = it },
                placeholder = {
                    Text(if (isCountUp) "Ví dụ: Ngày yêu, ngày phẫu thuật..." else "Ví dụ: Ngày nghỉ việc, sinh nhật...", color = AppTheme.colors.textMuted)
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

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val templates = if (isCountUp) {
                    listOf(
                        "❤️ Ngày yêu" to "❤️ Ngày yêu nhau",
                        "🏥 Phẫu thuật" to "🏥 Ngày phẫu thuật",
                        "🚭 Cai thuốc" to "🚭 Bắt đầu cai thuốc",
                        "👶 Bé ra đời" to "👶 Bé chào đời"
                    )
                } else {
                    listOf(
                        "🍉 Nghỉ lễ" to "🍉 Ngày nghỉ lễ",
                        "💼 Nghỉ việc" to "💼 Ngày nghỉ việc",
                        "📝 Ngày thi" to "📝 Ngày thi cử",
                        "✈️ Du lịch" to "✈️ Chuyến đi du lịch"
                    )
                }
                templates.forEach { (label, value) ->
                    QuickTemplateChip(
                        label = label,
                        onClick = { milestoneName = value }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Thời gian đến đích
            SectionLabel(text = if (isCountUp) "THỜI GIAN BẮT ĐẦU" else "THỜI GIAN ĐẾN ĐÍCH")
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

            // Cảnh báo thời gian
            if (!isTimeValid && targetDateMillis != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFB71C1C).copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFEF5350),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isCountUp) "Vui lòng chọn thời gian ở trong quá khứ." else "Thời gian đã qua! Vui lòng chọn lại thời gian hợp lệ.",
                        color = androidx.compose.ui.graphics.Color(0xFFEF5350),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
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
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notifyEnabled = true
                                    }
                                } else {
                                    notifyEnabled = enabled
                                }
                            },
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

            Spacer(Modifier.height(24.dp))

            // Ảnh nền sự kiện
            SectionLabel(text = "ẢNH NỀN SỰ KIỆN (TÙY CHỌN)")
            Spacer(Modifier.height(8.dp))

            BackgroundImagePickerCard(
                imagePath = backgroundImagePath,
                isProcessing = isProcessingImage,
                onPickImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = {
                    backgroundImagePath?.let {
                        com.nvdung1607.countdayleave.ui.utils.ImageStorageUtils.deleteImage(it)
                    }
                    backgroundImagePath = null
                }
            )

            Spacer(Modifier.height(40.dp))

            // Nút lưu
            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            milestoneName.trim(),
                            buildTargetEpoch(),
                            notifyTimes,
                            notifyEnabled,
                            isCountUp,
                            backgroundImagePath
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
                        text = if (isEditing) "💾  Lưu thay đổi" else if (isCountUp) "🚀  Bắt đầu theo dõi" else "🚀  Bắt đầu đếm ngược",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(16.dp))

                var showDeleteConfirm by remember { mutableStateOf(false) }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        containerColor = AppTheme.colors.surfaceCard,
                        title = {
                            Text("Xóa sự kiện?", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(
                                "Bạn có chắc chắn muốn xóa mốc thời gian này không?",
                                color = AppTheme.colors.textSecondary
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDelete()
                                }
                            ) {
                                Text("Xóa", color = AppTheme.colors.error, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Hủy", color = AppTheme.colors.textMuted)
                            }
                        }
                    )
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppTheme.colors.error
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Xóa",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Xóa sự kiện",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            Icon(
                imageVector = if (isEditing) Icons.Rounded.Edit else Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isEditing) "Sửa sự kiện" else "Thiết lập mốc thời gian",
            color = AppTheme.colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Đặt tên và mốc thời gian bạn muốn theo dõi",
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

@Composable
private fun QuickTemplateChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surfaceCard)
            .border(
                width = 1.dp,
                color = AppTheme.colors.textMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EventTypeSegmentedControl(
    isCountUp: Boolean,
    onTypeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.surfaceCard)
            .border(
                width = 1.dp,
                color = AppTheme.colors.surfaceElevated.copy(alpha = 0.8f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Đếm ngược
        SegmentedButton(
            title = "Đếm ngược",
            icon = Icons.Rounded.HourglassTop,
            isSelected = !isCountUp,
            modifier = Modifier.weight(1f),
            onClick = { onTypeChanged(false) }
        )

        // Ngày đã qua
        SegmentedButton(
            title = "Ngày đã qua",
            icon = Icons.Rounded.History,
            isSelected = isCountUp,
            modifier = Modifier.weight(1f),
            onClick = { onTypeChanged(true) }
        )
    }
}

@Composable
private fun SegmentedButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected)
                    Brush.horizontalGradient(
                        listOf(
                            AppTheme.colors.gradientStart,
                            AppTheme.colors.gradientEnd
                        )
                    )
                else
                    Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else AppTheme.colors.textMuted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BackgroundImagePickerCard(
    imagePath: String?,
    isProcessing: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    val bitmap = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) {
            try {
                BitmapFactory.decodeFile(imagePath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surfaceCard)
            .border(
                1.dp,
                AppTheme.colors.surfaceElevated.copy(alpha = 0.8f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        if (isProcessing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppTheme.colors.accentPurple,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Đang xử lý ảnh...",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        } else if (bitmap != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Image preview with overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = "Ảnh nền sự kiện",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Subtle dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                    Text(
                        text = "Ảnh nền đã chọn",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.accentPurple
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.accentPurple.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Đổi ảnh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onRemoveImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.error
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.error.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Xóa ảnh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPickImage)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    AppTheme.colors.gradientStart.copy(alpha = 0.2f),
                                    AppTheme.colors.gradientEnd.copy(alpha = 0.1f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = "Chọn ảnh",
                        tint = AppTheme.colors.accentPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chọn ảnh từ thư viện",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Làm ảnh nền mờ phía sau đồng hồ đếm ngược",
                        color = AppTheme.colors.textMuted,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

