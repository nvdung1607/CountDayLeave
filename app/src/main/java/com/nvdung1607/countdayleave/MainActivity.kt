package com.nvdung1607.countdayleave

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nvdung1607.countdayleave.notification.DailyNotificationReceiver.Companion.CHANNEL_ID
import com.nvdung1607.countdayleave.notification.NotificationScheduler
import com.nvdung1607.countdayleave.ui.screens.CelebrationScreen
import com.nvdung1607.countdayleave.ui.screens.CountdownScreen
import com.nvdung1607.countdayleave.ui.screens.EventListScreen
import com.nvdung1607.countdayleave.ui.screens.SetupScreen
import com.nvdung1607.countdayleave.ui.theme.CountDayLeaveTheme
import com.nvdung1607.countdayleave.viewmodel.CountdownViewModel
import com.nvdung1607.countdayleave.viewmodel.EventListViewModel
import kotlinx.coroutines.launch

// ---- Navigation routes ----
object Routes {
    const val EVENT_LIST  = "event_list"
    const val SETUP       = "setup"
    const val COUNTDOWN   = "countdown"
    const val CELEBRATION = "celebration"
    const val ADMIN       = "admin"
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

        // Cập nhật lại Widget khi mở ứng dụng
        com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgets(this)

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
                    intentAction = intent?.action,
                    intentEventId = intent?.getStringExtra(NotificationScheduler.EXTRA_EVENT_ID)
                )
            }
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
    intentAction: String?,
    intentEventId: String?,
    navController: NavHostController = rememberNavController()
) {
    // Handle deep link logic from notification
    LaunchedEffect(intentAction, intentEventId) {
        if (intentEventId != null) {
            when (intentAction) {
                "celebration" -> {
                    navController.navigate("${Routes.CELEBRATION}/$intentEventId") {
                        popUpTo(Routes.EVENT_LIST) { inclusive = false }
                    }
                }
                "countdown" -> {
                    navController.navigate("${Routes.COUNTDOWN}/$intentEventId") {
                        popUpTo(Routes.EVENT_LIST) { inclusive = false }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.EVENT_LIST,
        modifier = Modifier.fillMaxSize()
    ) {
        // ---- Event List Screen ----
        composable(Routes.EVENT_LIST) {
            val listViewModel: EventListViewModel = viewModel()
            val events by listViewModel.events.collectAsState()
            
            EventListScreen(
                events = events,
                onEventClick = { eventId ->
                    val event = events.find { it.id == eventId }
                    if (event != null && !event.isCountUp && event.targetEpochMillis <= System.currentTimeMillis()) {
                        navController.navigate("${Routes.CELEBRATION}/$eventId")
                    } else {
                        navController.navigate("${Routes.COUNTDOWN}/$eventId")
                    }
                },
                onAddEvent = {
                    navController.navigate(Routes.SETUP)
                },
                onDeleteEvent = { eventId ->
                    listViewModel.deleteEvent(eventId)
                },
                onAdminClick = {
                    navController.navigate(Routes.ADMIN)
                }
            )
        }

        // ---- Admin Panel Screen ----
        composable(Routes.ADMIN) {
            com.nvdung1607.countdayleave.ui.screens.AdminScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCelebration = { eventId ->
                    navController.navigate("${Routes.CELEBRATION}/$eventId") {
                        popUpTo(Routes.EVENT_LIST) { inclusive = false }
                    }
                }
            )
        }

        // ---- Setup Screen (Add/Edit) ----
        composable(
            route = "${Routes.SETUP}?eventId={eventId}",
            arguments = listOf(navArgument("eventId") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val viewModel: CountdownViewModel = viewModel(key = eventId ?: "new_event")
            val uiState by viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(eventId) {
                if (eventId != null) {
                    viewModel.loadEvent(eventId)
                }
            }

            SetupScreen(
                initialMilestoneName = uiState.milestoneName,
                initialTargetMillis = if (uiState.targetEpochMillis > 0) uiState.targetEpochMillis else null,
                initialNotifyTimes = uiState.notifyTimes,
                initialNotifyEnabled = uiState.notifyEnabled,
                initialIsCountUp = uiState.isCountUp,
                initialBackgroundImagePath = uiState.backgroundImagePath,
                isEditing = eventId != null,
                onSave = { name, targetMillis, notifyTimes, notifyEnabled, isCountUp, backgroundImagePath ->
                    coroutineScope.launch {
                        val savedId = viewModel.saveConfig(name, targetMillis, notifyTimes, notifyEnabled, isCountUp, backgroundImagePath)
                        if (eventId == null) {
                            navController.navigate("${Routes.COUNTDOWN}/$savedId") {
                                popUpTo(Routes.EVENT_LIST) { inclusive = false }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                onDelete = {
                    viewModel.deleteCurrentEvent()
                    navController.navigate(Routes.EVENT_LIST) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ---- Countdown screen (Details) ----
        composable(
            route = "${Routes.COUNTDOWN}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val viewModel: CountdownViewModel = viewModel(key = eventId)
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(eventId) {
                viewModel.loadEvent(eventId)
            }

            // Auto-navigate to CelebrationScreen when countdown finishes
            var previousIsFinished by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(uiState.isFinished) {
                if (uiState.isFinished && previousIsFinished == false && uiState.eventId.isNotEmpty()) {
                    navController.navigate("${Routes.CELEBRATION}/${uiState.eventId}") {
                        popUpTo("${Routes.COUNTDOWN}/${uiState.eventId}") { inclusive = true }
                    }
                }
                previousIsFinished = uiState.isFinished
            }

            CountdownScreen(
                uiState = uiState,
                onNavigateToSetup = {
                    navController.navigate("${Routes.SETUP}?eventId=$eventId")
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCelebration = {
                    navController.navigate("${Routes.CELEBRATION}/$eventId")
                }
            )
        }

        // ---- Celebration screen ----
        composable(
            route = "${Routes.CELEBRATION}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val viewModel: CountdownViewModel = viewModel(key = eventId)
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(eventId) {
                viewModel.loadEvent(eventId)
            }

            CelebrationScreen(
                milestoneName = uiState.milestoneName,
                onSetupNew = {
                    navController.navigate(Routes.EVENT_LIST) {
                        popUpTo(0) { inclusive = true }
                    }
                    navController.navigate(Routes.SETUP)
                },
                onNavigateToSetup = {
                    navController.navigate("${Routes.SETUP}?eventId=$eventId")
                },
                onBack = {
                    navController.navigate(Routes.EVENT_LIST) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
