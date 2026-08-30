package com.nvdung1607.countdayleave.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nvdung1607.countdayleave.data.CountdownDataStore
import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.notification.NotificationScheduler
import kotlinx.coroutines.flow.map
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

    /** Danh sách tất cả sự kiện, tự động sắp xếp: đang đếm ngược (gần nhất) -> ngày đã qua -> đã kết thúc. */
    val events: StateFlow<List<CountdownConfig>> = dataStore.eventsFlow
        .map { list ->
            val now = System.currentTimeMillis()
            list.sortedWith(
                compareBy<CountdownConfig> { config ->
                    when {
                        // 1. Sự kiện đang đếm ngược còn hạn (ưu tiên cao nhất - nhóm 0)
                        !config.isCountUp && config.targetEpochMillis > now -> 0
                        // 2. Sự kiện "ngày đã qua" (nhóm 1)
                        config.isCountUp -> 1
                        // 3. Sự kiện đếm ngược đã kết thúc (nhóm 2)
                        else -> 2
                    }
                }.thenBy { config ->
                    if (!config.isCountUp && config.targetEpochMillis > now) {
                        // Sự kiện sắp tới: cái nào gần nhất xếp lên đầu
                        config.targetEpochMillis
                    } else {
                        // Sự kiện đã qua/kết thúc: cái nào mới nhất xếp lên đầu
                        -config.targetEpochMillis
                    }
                }
            )
        }
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
            com.nvdung1607.countdayleave.widget.CountdownWidgetProvider.updateAllWidgetsForEvent(getApplication(), eventId)
        }
    }
}

