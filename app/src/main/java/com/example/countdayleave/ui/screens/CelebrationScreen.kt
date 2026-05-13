package com.example.countdayleave.ui.screens

import androidx.compose.animation.*
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
import com.example.countdayleave.ui.components.FireworksCanvas
import com.example.countdayleave.ui.theme.*

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundDeep)
    ) {
        // Fireworks layer
        FireworksCanvas(modifier = Modifier.fillMaxSize())

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

            // Settings button (top right)
            IconButton(
                onClick = onNavigateToSetup,
                modifier = Modifier
                    .align(Alignment.TopEnd)
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

        // Main content (centered)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Giant celebration emoji
            Text(
                text = "🎉",
                fontSize = 90.sp,
                modifier = Modifier.scale(emojiScale)
            )

            Spacer(Modifier.height(24.dp))

            // Glassmorphism card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AppTheme.colors.surfaceCard.copy(alpha = 0.85f),
                                AppTheme.colors.surfaceElevated.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(28.dp),
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
