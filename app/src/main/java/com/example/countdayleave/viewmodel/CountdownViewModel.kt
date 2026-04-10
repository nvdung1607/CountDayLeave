package com.example.countdayleave.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countdayleave.data.CountdownDataStore
import com.example.countdayleave.model.CountdownConfig
import com.example.countdayleave.notification.NotificationScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CountdownUiState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val milestoneName: String = "",
    val targetEpochMillis: Long = 0L,
    val days: Long = 0L,
    val hours: Long = 0L,
    val minutes: Long = 0L,
    val seconds: Long = 0L,
    val isFinished: Boolean = false,
    // Setup form state
    val notifyHour: Int = 8,
    val notifyMinute: Int = 0,
    val notifyEnabled: Boolean = true
)

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = CountdownDataStore(application)
    private val scheduler = NotificationScheduler(application)

    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val config = dataStore.configFlow.first()
            if (config != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isConfigured = true,
                        milestoneName = config.milestoneName,
                        targetEpochMillis = config.targetEpochMillis,
                        notifyHour = config.notifyHour,
                        notifyMinute = config.notifyMinute,
                        notifyEnabled = config.notifyEnabled
                    )
                }
                startTimer()
            } else {
                _uiState.update { it.copy(isLoading = false, isConfigured = false) }
            }
        }
    }

    /** Lưu config sau khi user setup xong. */
    fun saveConfig(
        milestoneName: String,
        targetEpochMillis: Long,
        notifyHour: Int,
        notifyMinute: Int,
        notifyEnabled: Boolean
    ) {
        viewModelScope.launch {
            val config = CountdownConfig(
                milestoneName = milestoneName,
                targetEpochMillis = targetEpochMillis,
                notifyHour = notifyHour,
                notifyMinute = notifyMinute,
                notifyEnabled = notifyEnabled
            )
            dataStore.saveConfig(config)
            _uiState.update {
                it.copy(
                    isConfigured = true,
                    milestoneName = milestoneName,
                    targetEpochMillis = targetEpochMillis,
                    notifyHour = notifyHour,
                    notifyMinute = notifyMinute,
                    notifyEnabled = notifyEnabled
                )
            }
            // Schedule hoặc cancel notification
            if (notifyEnabled) {
                scheduler.schedule(config)
            } else {
                scheduler.cancel()
            }
            startTimer()
        }
    }

    /** Xóa config và reset về màn hình setup. */
    fun resetConfig() {
        timerJob?.cancel()
        viewModelScope.launch {
            dataStore.clearConfig()
            scheduler.cancel()
            _uiState.update { CountdownUiState(isLoading = false, isConfigured = false) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val target = _uiState.value.targetEpochMillis
                val diff = target - now

                if (diff <= 0) {
                    _uiState.update {
                        it.copy(
                            days = 0, hours = 0, minutes = 0, seconds = 0,
                            isFinished = true
                        )
                    }
                    break
                }

                val totalSeconds = diff / 1000
                val days    = totalSeconds / 86400
                val hours   = (totalSeconds % 86400) / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                _uiState.update {
                    it.copy(
                        days = days,
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        isFinished = false
                    )
                }

                delay(1000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
