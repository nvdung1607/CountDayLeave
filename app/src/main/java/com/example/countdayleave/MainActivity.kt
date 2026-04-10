package com.example.countdayleave

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.countdayleave.notification.DailyNotificationReceiver.Companion.CHANNEL_ID
import com.example.countdayleave.ui.screens.CelebrationScreen
import com.example.countdayleave.ui.screens.CountdownScreen
import com.example.countdayleave.ui.screens.SetupScreen
import com.example.countdayleave.ui.theme.CountDayLeaveTheme
import com.example.countdayleave.viewmodel.CountdownViewModel
import java.util.Calendar

// ---- Navigation routes ----
object Routes {
    const val SETUP       = "setup"
    const val COUNTDOWN   = "countdown"
    const val CELEBRATION = "celebration"
}

class MainActivity : ComponentActivity() {

    // Permission launcher (Android 13+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel (required on API 26+)
        createNotificationChannel()

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            CountDayLeaveTheme {
                AppNavigation(
                    startDestination = resolveStartDestination()
                )
            }
        }
    }

    /**
     * Nếu notification tap với action "celebration", mở thẳng CelebrationScreen.
     * Nếu action "countdown", mở CountdownScreen.
     */
    private fun resolveStartDestination(): String {
        return when (intent?.action) {
            "celebration" -> Routes.CELEBRATION
            "countdown"   -> Routes.COUNTDOWN
            else          -> Routes.COUNTDOWN   // ViewModel sẽ navigate đúng màn hình
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nhắc nhở đếm ngược",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Nhắc nhở hằng ngày về mốc thời gian của bạn"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}

@Composable
fun AppNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val viewModel: CountdownViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Observe loading finish → navigate to correct screen
    LaunchedEffect(uiState.isLoading, uiState.isConfigured) {
        if (!uiState.isLoading) {
            if (!uiState.isConfigured) {
                navController.navigate(Routes.SETUP) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Auto-navigate to CelebrationScreen when countdown finishes
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && uiState.isConfigured) {
            navController.navigate(Routes.CELEBRATION) {
                popUpTo(Routes.COUNTDOWN) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startDestination == Routes.CELEBRATION) Routes.CELEBRATION else Routes.COUNTDOWN,
        modifier = Modifier.fillMaxSize()
    ) {
        // ---- Setup screen ----
        composable(Routes.SETUP) {
            SetupScreen(
                initialMilestoneName  = uiState.milestoneName,
                initialTargetMillis   = if (uiState.targetEpochMillis > 0) uiState.targetEpochMillis else null,
                initialNotifyHour     = uiState.notifyHour,
                initialNotifyMinute   = uiState.notifyMinute,
                initialNotifyEnabled  = uiState.notifyEnabled,
                isEditing             = uiState.isConfigured,
                onSave = { name, targetMillis, notifyHour, notifyMinute, notifyEnabled ->
                    viewModel.saveConfig(name, targetMillis, notifyHour, notifyMinute, notifyEnabled)
                    navController.navigate(Routes.COUNTDOWN) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        // ---- Countdown screen ----
        composable(Routes.COUNTDOWN) {
            CountdownScreen(
                uiState = uiState,
                onNavigateToSetup = {
                    navController.navigate(Routes.SETUP) {
                        popUpTo(Routes.COUNTDOWN) { inclusive = false }
                    }
                }
            )
        }

        // ---- Celebration screen ----
        composable(Routes.CELEBRATION) {
            CelebrationScreen(
                milestoneName = uiState.milestoneName,
                onSetupNew = {
                    viewModel.resetConfig()
                    navController.navigate(Routes.SETUP) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(Routes.SETUP) {
                        popUpTo(Routes.CELEBRATION) { inclusive = false }
                    }
                }
            )
        }
    }
}