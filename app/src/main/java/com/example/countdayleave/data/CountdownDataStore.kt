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
        private val KEY_NOTIFY_HOUR = intPreferencesKey("notify_hour")
        private val KEY_NOTIFY_MINUTE = intPreferencesKey("notify_minute")
        private val KEY_NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
        private val KEY_IS_CONFIGURED = booleanPreferencesKey("is_configured")
    }

    /** Flow phát ra config hiện tại. Null nếu chưa được thiết lập. */
    val configFlow: Flow<CountdownConfig?> = context.dataStore.data.map { prefs ->
        val isConfigured = prefs[KEY_IS_CONFIGURED] ?: false
        if (!isConfigured) return@map null
        CountdownConfig(
            milestoneName = prefs[KEY_MILESTONE_NAME] ?: "",
            targetEpochMillis = prefs[KEY_TARGET_EPOCH] ?: 0L,
            notifyHour = prefs[KEY_NOTIFY_HOUR] ?: 8,
            notifyMinute = prefs[KEY_NOTIFY_MINUTE] ?: 0,
            notifyEnabled = prefs[KEY_NOTIFY_ENABLED] ?: true
        )
    }

    /** Lưu config và đánh dấu đã thiết lập. */
    suspend fun saveConfig(config: CountdownConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MILESTONE_NAME] = config.milestoneName
            prefs[KEY_TARGET_EPOCH] = config.targetEpochMillis
            prefs[KEY_NOTIFY_HOUR] = config.notifyHour
            prefs[KEY_NOTIFY_MINUTE] = config.notifyMinute
            prefs[KEY_NOTIFY_ENABLED] = config.notifyEnabled
            prefs[KEY_IS_CONFIGURED] = true
        }
    }

    /** Xóa toàn bộ config (reset về lần đầu). */
    suspend fun clearConfig() {
        context.dataStore.edit { it.clear() }
    }
}
