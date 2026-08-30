package com.nvdung1607.countdayleave.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.nvdung1607.countdayleave.ui.utils.rememberAdaptiveLayoutInfo
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.nvdung1607.countdayleave.R
import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventListScreen(
    events: List<CountdownConfig>,
    onEventClick: (eventId: String) -> Unit,
    onAddEvent: () -> Unit,
    onDeleteEvent: (eventId: String) -> Unit,
    onAdminClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
    ) {
        // Ambient top glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppTheme.colors.gradientStart.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // ---- Top Bar ----
            EventListTopBar(onAdminClick = onAdminClick)

            val sortedEvents = remember(events) {
                events.sortedWith(
                    compareBy<CountdownConfig> { config ->
                        val now = System.currentTimeMillis()
                        when {
                            !config.isCountUp && config.targetEpochMillis > now -> 0
                            config.isCountUp -> 1
                            else -> 2
                        }
                    }.thenBy { config ->
                        val now = System.currentTimeMillis()
                        if (!config.isCountUp && config.targetEpochMillis > now) {
                            config.targetEpochMillis
                        } else {
                            -config.targetEpochMillis
                        }
                    }
                )
            }

            if (sortedEvents.isEmpty()) {
                // ---- Empty state ----
                EmptyState(onAddEvent = onAddEvent)
            } else {
                // ---- Adaptive Event grid/list ----
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedEvents, key = { it.id }) { event ->
                        EventCard(
                            config = event,
                            onClick = { onEventClick(event.id) },
                            onDelete = { onDeleteEvent(event.id) }
                        )
                    }
                }
            }
        }

        // ---- FAB ----
        FloatingActionButton(
            onClick = onAddEvent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding()
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = AppTheme.colors.gradientStart.copy(alpha = 0.5f),
                    spotColor = AppTheme.colors.gradientEnd.copy(alpha = 0.5f)
                ),
            containerColor = Color.Transparent,
            contentColor = AppTheme.colors.textPrimary,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Thêm sự kiện",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun EventListTopBar(onAdminClick: () -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo Đếm Ngày",
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = AppTheme.colors.gradientStart.copy(alpha = 0.4f),
                    spotColor = AppTheme.colors.gradientEnd.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                tapCount++
                if (tapCount >= 16) {
                    tapCount = 0
                    onAdminClick()
                }
            }
        ) {
            Text(
                text = "Đếm Ngày",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp,
                    brush = Brush.linearGradient(
                        listOf(
                            AppTheme.colors.gradientStart,
                            AppTheme.colors.accentPurpleLight,
                            AppTheme.colors.accentBlue,
                            AppTheme.colors.gradientEnd
                        )
                    )
                )
            )
            Text(
                text = "Sự kiện của tôi",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun EventCard(
    config: CountdownConfig,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isFinished: Boolean
    val totalSeconds: Long
    
    if (config.isCountUp) {
        isFinished = false
        val countUpDiff = now - config.targetEpochMillis
        val diffToUse = if (countUpDiff > 0) countUpDiff else 0L
        totalSeconds = diffToUse / 1000
    } else {
        val diff = config.targetEpochMillis - now
        isFinished = diff <= 0
        totalSeconds = if (diff > 0) diff / 1000 else 0L
    }

    val days    = totalSeconds / 86400
    val hours   = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60

    val targetDate = remember(config.targetEpochMillis) {
        SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())
            .format(Date(config.targetEpochMillis))
    }

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
                    "Bạn có chắc muốn xóa \"${config.milestoneName}\" không?",
                    color = AppTheme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Xóa", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy", color = AppTheme.colors.textMuted)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFinished) 3.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isFinished)
                    Color.Transparent
                else
                    AppTheme.colors.gradientStart.copy(alpha = 0.2f),
                spotColor = if (isFinished) Color.Transparent else AppTheme.colors.gradientStart.copy(alpha = 0.15f)
            )
            .graphicsLayer {
                alpha = if (isFinished) 0.65f else 1f
            }
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.surfaceCard)
            .clickable(onClick = onClick)
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    if (isFinished)
                        Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50)))
                    else
                        Brush.verticalGradient(listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd))
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji / Status indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.radialGradient(
                            if (isFinished)
                                listOf(Color(0xFF4CAF50).copy(alpha = 0.2f), Color.Transparent)
                            else
                                listOf(AppTheme.colors.gradientStart.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFinished) Icons.Rounded.Celebration else Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = if (isFinished) Color(0xFF4CAF50) else AppTheme.colors.accentPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.milestoneName,
                    color = if (isFinished) AppTheme.colors.textSecondary else AppTheme.colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "🎯  $targetDate",
                    color = AppTheme.colors.textMuted,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))

                if (isFinished) {
                    Text(
                        text = "✅  Đã hoàn thành!",
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    // Countdown preview
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CountdownChip(value = days, label = "ngày")
                        CountdownChip(value = hours, label = "giờ")
                        CountdownChip(value = minutes, label = "phút")
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Xóa",
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CountdownChip(value: Long, label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.surfaceElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${value} ${label}",
            color = AppTheme.colors.accentPurpleLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyState(onAddEvent: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = null,
            tint = AppTheme.colors.textMuted,
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer { this.alpha = alpha }
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Chưa có sự kiện nào",
            color = AppTheme.colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Hãy thêm mốc thời gian đầu tiên\nđể bắt đầu theo dõi!",
            color = AppTheme.colors.textMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onAddEvent,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = AppTheme.colors.textPrimary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd)
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm sự kiện", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

