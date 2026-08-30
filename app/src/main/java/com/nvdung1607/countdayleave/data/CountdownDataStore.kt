package com.nvdung1607.countdayleave.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.model.NotifyTime
import java.io.IOException
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "countdown_prefs")

/**
 * DataStore hỗ trợ lưu trữ NHIỀU sự kiện.
 * Định dạng lưu: JSON array string trong một key duy nhất "events_json".
 * Backward-compatible: tự động migrate dữ liệu 1-sự-kiện cũ sang list mới.
 */
class CountdownDataStore(private val context: Context) {

    companion object {
        // Key cho danh sách events (JSON array)
        private val KEY_EVENTS_JSON = stringPreferencesKey("events_json")

        // Legacy keys (chỉ dùng để migration)
        private val KEY_MILESTONE_NAME = stringPreferencesKey("milestone_name")
        private val KEY_TARGET_EPOCH   = longPreferencesKey("target_epoch_millis")
        private val KEY_NOTIFY_TIMES   = stringPreferencesKey("notify_times")
        private val KEY_NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
        private val KEY_IS_CONFIGURED  = booleanPreferencesKey("is_configured")
    }

    /** Flow phát ra danh sách toàn bộ sự kiện. */
    val eventsFlow: Flow<List<CountdownConfig>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val json = prefs[KEY_EVENTS_JSON]
            if (json != null) {
                parseEventsJson(json)
            } else {
                // Migration: nếu có dữ liệu cũ, chuyển sang list
                migrateFromLegacy(prefs)
            }
        }

    /** Lưu hoặc cập nhật một sự kiện (upsert theo id). */
    suspend fun saveEvent(config: CountdownConfig) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_EVENTS_JSON]?.let { parseEventsJson(it) } ?: emptyList()
            val updated = current.toMutableList()
            val idx = updated.indexOfFirst { it.id == config.id }
            if (idx >= 0) updated[idx] = config else updated.add(config)
            prefs[KEY_EVENTS_JSON] = serializeEventsJson(updated)
        }
    }

    /** Xóa một sự kiện theo id. */
    suspend fun deleteEvent(eventId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_EVENTS_JSON]?.let { parseEventsJson(it) } ?: emptyList()
            val updated = current.filter { it.id != eventId }
            prefs[KEY_EVENTS_JSON] = serializeEventsJson(updated)
        }
    }

    /** Xóa toàn bộ dữ liệu (reset). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // ---- Serialization ----

    private fun serializeEventsJson(events: List<CountdownConfig>): String {
        val arr = JSONArray()
        events.forEach { config ->
            val obj = JSONObject().apply {
                put("id", config.id)
                put("milestoneName", config.milestoneName)
                put("targetEpochMillis", config.targetEpochMillis)
                put("notifyEnabled", config.notifyEnabled)
                put("isCountUp", config.isCountUp)
                val timesArr = JSONArray()
                config.notifyTimes.forEach { t ->
                    timesArr.put(JSONObject().apply {
                        put("hour", t.hour)
                        put("minute", t.minute)
                    })
                }
                put("notifyTimes", timesArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseEventsJson(json: String): List<CountdownConfig> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                try {
                    val obj = arr.getJSONObject(i)
                    val timesArr = obj.optJSONArray("notifyTimes")
                    val times = if (timesArr != null) {
                        (0 until timesArr.length()).mapNotNull { j ->
                            val t = timesArr.getJSONObject(j)
                            NotifyTime(t.getInt("hour"), t.getInt("minute"))
                        }
                    } else emptyList()
                    CountdownConfig(
                        id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                        milestoneName = obj.getString("milestoneName"),
                        targetEpochMillis = obj.getLong("targetEpochMillis"),
                        notifyTimes = times,
                        notifyEnabled = obj.optBoolean("notifyEnabled", true),
                        isCountUp = obj.optBoolean("isCountUp", false)
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    /** Đọc dữ liệu format cũ và trả về list 1 phần tử (để migrate). */
    private fun migrateFromLegacy(prefs: Preferences): List<CountdownConfig> {
        val isConfigured = prefs[KEY_IS_CONFIGURED] ?: false
        if (!isConfigured) return emptyList()

        val notifyTimesStr = prefs[KEY_NOTIFY_TIMES]
        val times = if (notifyTimesStr != null && notifyTimesStr.isNotBlank()) {
            notifyTimesStr.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull(); val m = parts[1].toIntOrNull()
                    if (h != null && m != null) NotifyTime(h, m) else null
                } else null
            }
        } else listOf(NotifyTime(8, 0))

        return listOf(
            CountdownConfig(
                id = UUID.randomUUID().toString(),
                milestoneName = prefs[KEY_MILESTONE_NAME] ?: "",
                targetEpochMillis = prefs[KEY_TARGET_EPOCH] ?: 0L,
                notifyTimes = times,
                notifyEnabled = prefs[KEY_NOTIFY_ENABLED] ?: true
            )
        )
    }
}

