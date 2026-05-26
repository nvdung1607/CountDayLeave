package com.example.countdayleave.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countdayleave.data.CountdownDataStore
import com.example.countdayleave.model.CountdownConfig
import com.example.countdayleave.notification.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình danh sách sự kiện (EventListScreen).
 * Expose danh sách toàn bộ events và các action thêm/xóa.
 */
class EventListViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = CountdownDataStore(application)
    private val scheduler = NotificationScheduler(application)

    /** Danh sách tất cả sự kiện, cập nhật realtime. */
    val events: StateFlow<List<CountdownConfig>> = dataStore.eventsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Xóa một sự kiện và huỷ alarm tương ứng. */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            scheduler.cancel(eventId)
            dataStore.deleteEvent(eventId)
            com.example.countdayleave.widget.CountdownWidgetProvider.updateAllWidgetsForEvent(getApplication(), eventId)
        }
    }
}
