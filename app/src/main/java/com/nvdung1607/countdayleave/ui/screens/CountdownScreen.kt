package com.nvdung1607.countdayleave.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import com.nvdung1607.countdayleave.ui.utils.ShareUtils
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.nvdung1607.countdayleave.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvdung1607.countdayleave.ui.theme.*
import com.nvdung1607.countdayleave.viewmodel.CountdownUiState
import java.text.SimpleDateFormat
import java.util.*

import com.nvdung1607.countdayleave.ui.utils.rememberAdaptiveLayoutInfo
import com.nvdung1607.countdayleave.ui.utils.WindowWidthClass
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CountdownScreen(
    uiState: CountdownUiState,
    onNavigateToSetup: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val notifyEnabled = uiState.notifyEnabled
    val adaptiveInfo = rememberAdaptiveLayoutInfo()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
    ) {
        // Ambient glow background
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppTheme.colors.gradientStart.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val view = LocalView.current

            var showShareDialog by remember { mutableStateOf(false) }
            var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

            if (showShareDialog && capturedBitmap != null) {
                ShareOptionsDialog(
                    onDismiss = { showShareDialog = false },
                    onShareClick = {
                        ShareUtils.shareBitmap(context, capturedBitmap!!, "Chia sẻ mốc thời gian")
                    },
                    onSaveClick = {
                        coroutineScope.launch {
                            val success = ShareUtils.saveImageToGallery(context, capturedBitmap!!)
                            if (success) {
                                android.widget.Toast.makeText(context, "Đã lưu ảnh vào thư viện!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Lưu ảnh thất bại!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveInfo.maxContentWidth)
                    .then(
                        if (adaptiveInfo.isCompactHeight || adaptiveInfo.fontScale > 1.15f || adaptiveInfo.isLandscape) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with back button
                TopBar(
                    onBackClick = onNavigateBack,
                    onSettingsClick = onNavigateToSetup,
                    onShareClick = {
                        try {
                            val bitmap = android.graphics.Bitmap.createBitmap(
                                view.width,
                                view.height,
                                android.graphics.Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bitmap)
                            view.draw(canvas)
                            capturedBitmap = bitmap
                            showShareDialog = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "Không thể tạo ảnh chia sẻ!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Shareable Card Column containing Title, Date and Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AppTheme.colors.surfaceCard,
                                    AppTheme.colors.backgroundDark
                                )
                            )
                        )
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tên mốc thời gian
                    MilestoneTitle(name = uiState.milestoneName)

                    Spacer(Modifier.height(8.dp))

                    // Thời gian đến đích
                    val targetDate = remember(uiState.targetEpochMillis) {
                        SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())
                            .format(Date(uiState.targetEpochMillis))
                    }
                    Text(
                        text = "🎯  $targetDate",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    // Countdown grid
                    CountdownGrid(
                        days    = uiState.days,
                        hours   = uiState.hours,
                        minutes = uiState.minutes,
                        seconds = uiState.seconds,
                        useFourInRow = adaptiveInfo.isLandscape && adaptiveInfo.screenWidthDp >= 560.dp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Motivation Quote Card
                val dateKey = remember { java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date()) }
                var quoteIndex by remember(uiState.eventId, dateKey) {
                    mutableStateOf(
                        context.getSharedPreferences("widget_prefs", android.content.Context.MODE_PRIVATE)
                            .getInt("quote_index_$dateKey", com.nvdung1607.countdayleave.data.QuoteRepository.getQuoteOfTheDayIndex(context))
                    )
                }
                val quote = remember(quoteIndex) { com.nvdung1607.countdayleave.data.QuoteRepository.getQuote(context, quoteIndex) }
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(quote))
                            android.widget.Toast.makeText(context, "Đã sao chép câu danh ngôn vào bộ nhớ tạm! 💡", android.widget.Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.surfaceCard.copy(alpha = 0.6f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                AppTheme.colors.gradientStart.copy(alpha = 0.2f),
                                AppTheme.colors.gradientEnd.copy(alpha = 0.1f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 4.dp), // Pushes buttons closer to edges
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val count = com.nvdung1607.countdayleave.data.QuoteRepository.getQuotesCount(context)
                                val newIndex = (quoteIndex - 1 + count) % count
                                quoteIndex = newIndex
                                context.getSharedPreferences("widget_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("quote_index_$dateKey", newIndex)
                                    .apply()
                                com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgets(context)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Quote trước",
                                tint = AppTheme.colors.textSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = AppTheme.colors.accentPurpleLight,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("“ ")
                                }
                                append(quote)
                                withStyle(
                                    style = SpanStyle(
                                        color = AppTheme.colors.accentPurpleLight,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(" ”")
                                }
                            },
                            color = AppTheme.colors.textPrimary.copy(alpha = 0.9f),
                            fontSize = 15.sp, // Larger quote size
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                val count = com.nvdung1607.countdayleave.data.QuoteRepository.getQuotesCount(context)
                                val newIndex = (quoteIndex + 1) % count
                                quoteIndex = newIndex
                                context.getSharedPreferences("widget_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("quote_index_$dateKey", newIndex)
                                    .apply()
                                com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgets(context)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForwardIos,
                                contentDescription = "Quote sau",
                                tint = AppTheme.colors.textSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Footer hint — chỉ hiển thị khi thông báo đang bật
                if (notifyEnabled) {
                    Text(
                        text = "Hằng ngày bạn sẽ nhận được nhắc nhở\ncho đến khi đến đích 🚀",
                        color = AppTheme.colors.textMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ---- Top bar with back ----
@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
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



        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Share button
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceCard)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Chia sẻ",
                    tint = AppTheme.colors.accentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceCard)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Chỉnh sửa",
                    tint = AppTheme.colors.accentPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ---- Milestone title ----
@Composable
private fun MilestoneTitle(name: String) {
    Text(
        text = name,
        color = AppTheme.colors.textPrimary,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        lineHeight = 36.sp
    )
}

// ---- Countdown 2x2 grid / 4-in-a-row grid ----
@Composable
private fun CountdownGrid(
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long,
    useFourInRow: Boolean = false
) {
    if (useFourInRow) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownCard(value = days,    label = "NGÀY", modifier = Modifier.weight(1f), isMain = true)
            CountdownCard(value = hours,   label = "GIỜ",  modifier = Modifier.weight(1f), isMain = true)
            CountdownCard(value = minutes, label = "PHÚT", modifier = Modifier.weight(1f))
            CountdownCard(value = seconds, label = "GIÂY", modifier = Modifier.weight(1f), isSeconds = true)
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CountdownCard(value = days,    label = "NGÀY", modifier = Modifier.weight(1f), isMain = true)
                CountdownCard(value = hours,   label = "GIỜ",  modifier = Modifier.weight(1f), isMain = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CountdownCard(value = minutes, label = "PHÚT", modifier = Modifier.weight(1f))
                CountdownCard(value = seconds, label = "GIÂY", modifier = Modifier.weight(1f), isSeconds = true)
            }
        }
    }
}

// ---- Individual card ----
@Composable
private fun CountdownCard(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    isMain: Boolean = false,
    isSeconds: Boolean = false
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val fontScale = density.fontScale

    val animatedValue by animateIntAsState(
        targetValue = value.toInt(),
        animationSpec = tween(durationMillis = 300, easing = EaseOut),
        label = "countdown_$label"
    )

    val cardBackground = if (isMain)
        Brush.linearGradient(listOf(AppTheme.colors.gradientStart.copy(alpha = 0.25f), AppTheme.colors.gradientEnd.copy(alpha = 0.15f)))
    else
        Brush.linearGradient(listOf(AppTheme.colors.surfaceCard, AppTheme.colors.surfaceCard))

    val targetFontSize = remember(isMain, fontScale) {
        val baseSp = if (isMain) 52f else 38f
        val scaledSp = if (fontScale > 1.2f) baseSp / (fontScale * 0.8f) else baseSp
        scaledSp.sp
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isMain) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isMain) AppTheme.colors.gradientStart.copy(alpha = 0.3f) else Color.Transparent,
                spotColor = if (isMain) AppTheme.colors.gradientEnd.copy(alpha = 0.3f) else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.backgroundDark) // SOLID BACKGROUND to hide shadow bleeding
            .background(cardBackground)
            .padding(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(
                    if (isMain)
                        Brush.linearGradient(listOf(AppTheme.colors.gradientStart.copy(alpha = 0.12f), AppTheme.colors.gradientEnd.copy(alpha = 0.08f)))
                    else
                        Brush.linearGradient(listOf(AppTheme.colors.surfaceCard, AppTheme.colors.surfaceCard))
                )
                .padding(vertical = if (isMain) 24.dp else 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = String.format("%02d", animatedValue),
                transitionSpec = {
                    slideInVertically { -it } + fadeIn() togetherWith
                    slideOutVertically { it } + fadeOut()
                },
                label = "num_$label"
            ) { displayValue ->
                Text(
                    text = displayValue,
                    color = if (isSeconds) AppTheme.colors.accentBlue
                             else if (isMain) AppTheme.colors.textPrimary
                             else AppTheme.colors.accentPurpleLight,
                    fontSize = targetFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = AppTheme.colors.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppTheme.colors.backgroundDark,
        title = {
            Text(
                text = "Tuỳ chọn chia sẻ",
                color = AppTheme.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Option 1: Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.surfaceCard)
                        .clickable { onShareClick(); onDismiss() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = AppTheme.colors.accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Chia sẻ ảnh",
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Gửi qua Messenger, Zalo, Story...",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Option 2: Save
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.surfaceCard)
                        .clickable { onSaveClick(); onDismiss() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = AppTheme.colors.accentPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Lưu ảnh về máy",
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tải ảnh về thư viện thiết bị",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Đóng",
                    color = AppTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

