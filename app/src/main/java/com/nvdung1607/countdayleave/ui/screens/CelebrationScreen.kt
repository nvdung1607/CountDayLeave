package com.nvdung1607.countdayleave.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import com.nvdung1607.countdayleave.ui.utils.ShareUtils
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nvdung1607.countdayleave.ui.components.FireworksCanvas
import com.nvdung1607.countdayleave.ui.theme.*

@Composable
fun CelebrationScreen(
    milestoneName: String,
    onSetupNew: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    // Emoji bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        // 1. Play sound
        try {
            val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Periodic Haptic vibrations (6 times, matching fireworks bursts at 750ms spacing)
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            for (i in 0 until 6) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(80L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80L)
                }
                kotlinx.coroutines.delay(750L)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
    ) {
        // Fireworks layer
        FireworksCanvas(modifier = Modifier.fillMaxSize())

        val coroutineScope = rememberCoroutineScope()
        val view = LocalView.current

        var showShareDialog by remember { mutableStateOf(false) }
        var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

        if (showShareDialog && capturedBitmap != null) {
            ShareOptionsDialog(
                onDismiss = { showShareDialog = false },
                onShareClick = {
                    ShareUtils.shareBitmap(context, capturedBitmap!!, "Chia sẻ ngày chiến thắng")
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

        // Top Buttons (Back and Settings)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(20.dp)
        ) {
            // Back button (top left)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceCard.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Quay lại",
                    tint = AppTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Settings & Share buttons (top right)
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share button
                IconButton(
                    onClick = {
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
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceCard.copy(alpha = 0.8f))
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
                    onClick = onNavigateToSetup,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceCard.copy(alpha = 0.8f))
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

        // Main content (centered)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shareable Card containing Emoji and Glassmorphism card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AppTheme.colors.surfaceCard,
                                AppTheme.colors.backgroundDark
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Giant celebration emoji
                Text(
                    text = "🎉",
                    fontSize = 90.sp,
                    modifier = Modifier.scale(emojiScale)
                )

                Spacer(Modifier.height(24.dp))

                // Inner Glassmorphism congratulations card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    AppTheme.colors.surfaceCard.copy(alpha = 0.5f),
                                    AppTheme.colors.surfaceElevated.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chúc mừng!",
                            color = AppTheme.colors.accentBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Đã đến ngày",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = milestoneName,
                            color = AppTheme.colors.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // Nút thiết lập mốc mới
            Button(
                onClick = onSetupNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                            Brush.horizontalGradient(listOf(AppTheme.colors.gradientStart, AppTheme.colors.gradientEnd)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🎯  Thiết lập mốc mới",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

