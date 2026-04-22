package com.example.countdayleave.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.countdayleave.model.CountdownConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "countdown_prefs")

class CountdownDataStore(private val context: Context) {

    companion object {
        private val KEY_MILESTONE_NAME = stringPreferencesKey("milestone_name")
        private val KEY_TARGET_EPOCH = longPreferencesKey("target_epoch_millis")
        private val KEY_NOTIFY_TIMES = stringPreferencesKey("notify_times")
        private val KEY_NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
        private val KEY_IS_CONFIGURED = booleanPreferencesKey("is_configured")
    }

    /** Flow phát ra config hiện tại. Null nếu chưa được thiết lập. */
    val configFlow: Flow<CountdownConfig?> = context.dataStore.data.map { prefs ->
        val isConfigured = prefs[KEY_IS_CONFIGURED] ?: false
        if (!isConfigured) return@map null
        
        // Legacy keys for migration
        val keyNotifyHour = intPreferencesKey("notify_hour")
        val keyNotifyMinute = intPreferencesKey("notify_minute")

        val notifyTimesStr = prefs[KEY_NOTIFY_TIMES]
        
        val notifyTimesList = if (notifyTimesStr != null) {
            if (notifyTimesStr.isBlank()) {
                emptyList()
            } else {
                notifyTimesStr.split(",").mapNotNull {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        val h = parts[0].toIntOrNull()
                        val m = parts[1].toIntOrNull()
                        if (h != null && m != null) com.example.countdayleave.model.NotifyTime(h, m) else null
                    } else null
                }
            }
        } else {
            // Try migration
            val oldHour = prefs[keyNotifyHour]
            val oldMinute = prefs[keyNotifyMinute]
            if (oldHour != null && oldMinute != null) {
                listOf(com.example.countdayleave.model.NotifyTime(oldHour, oldMinute))
            } else {
                listOf(com.example.countdayleave.model.NotifyTime(8, 0))
            }
        }
        
        CountdownConfig(
            milestoneName = prefs[KEY_MILESTONE_NAME] ?: "",
            targetEpochMillis = prefs[KEY_TARGET_EPOCH] ?: 0L,
            notifyTimes = notifyTimesList,
            notifyEnabled = prefs[KEY_NOTIFY_ENABLED] ?: true
        )
    }

    /** Lưu config và đánh dấu đã thiết lập. */
    suspend fun saveConfig(config: CountdownConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MILESTONE_NAME] = config.milestoneName
            prefs[KEY_TARGET_EPOCH] = config.targetEpochMillis
            
            val timesStr = config.notifyTimes.joinToString(",") { 
                String.format("%02d:%02d", it.hour, it.minute) 
            }
            prefs[KEY_NOTIFY_TIMES] = timesStr
            
            prefs[KEY_NOTIFY_ENABLED] = config.notifyEnabled
            prefs[KEY_IS_CONFIGURED] = true
        }
    }

    /** Xóa toàn bộ config (reset về lần đầu). */
    suspend fun clearConfig() {
        context.dataStore.edit { it.clear() }
    }
}
