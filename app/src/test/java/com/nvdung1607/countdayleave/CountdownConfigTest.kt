package com.nvdung1607.countdayleave

import com.nvdung1607.countdayleave.model.CountdownConfig
import com.nvdung1607.countdayleave.model.NotifyTime
import org.junit.Assert.*
import org.junit.Test

class CountdownConfigTest {

    @Test
    fun testCountdownConfigDefaults() {
        val config = CountdownConfig(
            milestoneName = "Ngày nghỉ việc",
            targetEpochMillis = 1800000000000L,
            notifyTimes = listOf(NotifyTime(8, 0)),
            notifyEnabled = true
        )

        assertNotNull(config.id)
        assertTrue(config.id.isNotEmpty())
        assertEquals("Ngày nghỉ việc", config.milestoneName)
        assertEquals(1800000000000L, config.targetEpochMillis)
        assertEquals(1, config.notifyTimes.size)
        assertEquals(8, config.notifyTimes[0].hour)
        assertEquals(0, config.notifyTimes[0].minute)
        assertTrue(config.notifyEnabled)
        assertFalse(config.isCountUp)
    }

    @Test
    fun testNotifyTimeFormatting() {
        val notifyTime = NotifyTime(hour = 7, minute = 5)
        val formatted = String.format("%02d:%02d", notifyTime.hour, notifyTime.minute)
        assertEquals("07:05", formatted)
    }

    @Test
    fun testCountUpFlag() {
        val config = CountdownConfig(
            milestoneName = "Kỷ niệm 1 năm",
            targetEpochMillis = 1700000000000L,
            notifyTimes = emptyList(),
            notifyEnabled = false,
            isCountUp = true
        )
        assertTrue(config.isCountUp)
    }
}
