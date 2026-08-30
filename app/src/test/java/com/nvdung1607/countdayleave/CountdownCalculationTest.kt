package com.nvdung1607.countdayleave

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownCalculationTest {

    data class TimeUnits(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

    private fun calculateCountdown(targetMillis: Long, currentMillis: Long): TimeUnits {
        val diff = targetMillis - currentMillis
        if (diff <= 0) return TimeUnits(0, 0, 0, 0)
        val totalSeconds = diff / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return TimeUnits(days, hours, minutes, seconds)
    }

    private fun calculateCountUp(startMillis: Long, currentMillis: Long): TimeUnits {
        val diff = currentMillis - startMillis
        if (diff <= 0) return TimeUnits(0, 0, 0, 0)
        val totalSeconds = diff / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return TimeUnits(days, hours, minutes, seconds)
    }

    @Test
    fun testExactDayCalculation() {
        val now = 1000000000L
        // 2 days = 2 * 86400 * 1000 = 172,800,000 ms
        val target = now + 172800000L
        val result = calculateCountdown(target, now)
        assertEquals(2L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(0L, result.seconds)
    }

    @Test
    fun testMixedTimeUnits() {
        val now = 1000000000L
        // 1 day + 2 hours + 30 minutes + 45 seconds = 86400 + 7200 + 1800 + 45 = 95,445 seconds
        val target = now + 95445000L
        val result = calculateCountdown(target, now)
        assertEquals(1L, result.days)
        assertEquals(2L, result.hours)
        assertEquals(30L, result.minutes)
        assertEquals(45L, result.seconds)
    }

    @Test
    fun testExpiredCountdownReturnsZero() {
        val now = 1000000000L
        val pastTarget = now - 50000L
        val result = calculateCountdown(pastTarget, now)
        assertEquals(0L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(0L, result.seconds)
    }

    @Test
    fun testCountUpCorrectlyCalculatesElapsed() {
        val start = 1000000000L
        // 5 days elapsed
        val now = start + (5 * 86400000L) + (3 * 3600000L)
        val result = calculateCountUp(start, now)
        assertEquals(5L, result.days)
        assertEquals(3L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(0L, result.seconds)
    }
}
