package com.nvdung1607.countdayleave.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
fun CountdownScreen(
    uiState: CountdownUiState,
    onNavigateToSetup: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val notifyEnabled = uiState.notifyEnabled
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
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            TopBar(
                onBackClick = onNavigateBack,
                onSettingsClick = onNavigateToSetup
            )

            Spacer(Modifier.weight(0.5f))

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

            Spacer(Modifier.height(40.dp))

            // Countdown grid
            CountdownGrid(
                days    = uiState.days,
                hours   = uiState.hours,
                minutes = uiState.minutes,
                seconds = uiState.seconds
            )

            Spacer(Modifier.height(32.dp))

             // Motivation Quote Card
            val context = androidx.compose.ui.platform.LocalContext.current
            val dateKey = remember { java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date()) }
            var quoteIndex by remember(uiState.eventId, dateKey) {
                mutableStateOf(
                    context.getSharedPreferences("widget_prefs", android.content.Context.MODE_PRIVATE)
                        .getInt("quote_index_$dateKey", com.nvdung1607.countdayleave.data.QuoteRepository.getQuoteOfTheDayIndex(context))
                )
            }
            val quote = remember(quoteIndex) { com.nvdung1607.countdayleave.data.QuoteRepository.getQuote(context, quoteIndex) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
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

            Spacer(Modifier.weight(1f))

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

// ---- Top bar with back ----
@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
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
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Đếm Ngày",
                style = TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.5).sp,
                    brush = Brush.linearGradient(
                        listOf(
                            AppTheme.colors.gradientStart,
                            AppTheme.colors.accentPurpleLight,
                            AppTheme.colors.gradientEnd
                        )
                    )
                )
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

// ---- Countdown 2x2 grid ----
@Composable
private fun CountdownGrid(
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long
) {
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

// ---- Individual card ----
@Composable
private fun CountdownCard(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    isMain: Boolean = false,
    isSeconds: Boolean = false
) {
    val animatedValue by animateIntAsState(
        targetValue = value.toInt(),
        animationSpec = tween(durationMillis = 300, easing = EaseOut),
        label = "countdown_$label"
    )

    val cardBackground = if (isMain)
        Brush.linearGradient(listOf(AppTheme.colors.gradientStart.copy(alpha = 0.25f), AppTheme.colors.gradientEnd.copy(alpha = 0.15f)))
    else
        Brush.linearGradient(listOf(AppTheme.colors.surfaceCard, AppTheme.colors.surfaceCard))

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isMain) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isMain) AppTheme.colors.gradientStart.copy(alpha = 0.3f) else Color.Transparent,
                spotColor = if (isMain) AppTheme.colors.gradientEnd.copy(alpha = 0.3f) else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
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
                .padding(vertical = if (isMain) 28.dp else 20.dp),
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
                    fontSize = if (isMain) 52.sp else 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = AppTheme.colors.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}

