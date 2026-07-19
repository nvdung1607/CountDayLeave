package com.nvdung1607.countdayleave.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nvdung1607.countdayleave.data.CountdownDataStore
import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.model.NotifyTime
import com.nvdung1607.countdayleave.notification.NotificationScheduler
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
    val eventId: String = "",
    val milestoneName: String = "",
    val targetEpochMillis: Long = 0L,
    val days: Long = 0L,
    val hours: Long = 0L,
    val minutes: Long = 0L,
    val seconds: Long = 0L,
    val isFinished: Boolean = false,
    val notifyTimes: List<NotifyTime> = listOf(NotifyTime(8, 0)),
    val notifyEnabled: Boolean = true
)

/**
 * ViewModel cho màn hình đếm ngược chi tiết của MỘT sự kiện.
 * Nhận [eventId] để load đúng event từ DataStore.
 */
class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = CountdownDataStore(application)
    private val scheduler = NotificationScheduler(application)

    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    /** Gọi sau khi ViewModel được tạo, truyền eventId cần load. */
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val events = dataStore.eventsFlow.first()
            val config = events.find { it.id == eventId }
            if (config != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        eventId = config.id,
                        milestoneName = config.milestoneName,
                        targetEpochMillis = config.targetEpochMillis,
                        notifyTimes = config.notifyTimes,
                        notifyEnabled = config.notifyEnabled
                    )
                }
                startTimer()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Lưu/cập nhật sự kiện này. */
    fun saveConfig(
        milestoneName: String,
        targetEpochMillis: Long,
        notifyTimes: List<NotifyTime>,
        notifyEnabled: Boolean
    ) {
        viewModelScope.launch {
            val currentId = _uiState.value.eventId.ifBlank {
                java.util.UUID.randomUUID().toString()
            }
            val config = CountdownConfig(
                id = currentId,
                milestoneName = milestoneName,
                targetEpochMillis = targetEpochMillis,
                notifyTimes = notifyTimes,
                notifyEnabled = notifyEnabled
            )
            dataStore.saveEvent(config)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    eventId = currentId,
                    milestoneName = milestoneName,
                    targetEpochMillis = targetEpochMillis,
                    notifyTimes = notifyTimes,
                    notifyEnabled = notifyEnabled
                )
            }
            if (notifyEnabled && targetEpochMillis > System.currentTimeMillis()) {
                scheduler.schedule(config)
            } else {
                scheduler.cancel(currentId)
            }
            com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgetsForEvent(getApplication(), currentId)
            startTimer()
        }
    }

    /** Xóa sự kiện hiện tại. */
    fun deleteCurrentEvent() {
        timerJob?.cancel()
        viewModelScope.launch {
            val id = _uiState.value.eventId
            if (id.isNotBlank()) {
                scheduler.cancel(id)
                dataStore.deleteEvent(id)
                com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgetsForEvent(getApplication(), id)
            }
            _uiState.update { CountdownUiState(isLoading = false) }
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
                        it.copy(days = 0, hours = 0, minutes = 0, seconds = 0, isFinished = true)
                    }
                    break
                }

                val totalSeconds = diff / 1000
                _uiState.update {
                    it.copy(
                        days    = totalSeconds / 86400,
                        hours   = (totalSeconds % 86400) / 3600,
                        minutes = (totalSeconds % 3600) / 60,
                        seconds = totalSeconds % 60,
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

