package com.nvdung1607.countdayleave

import com.nvdung1607.countdayleave.model.CountdownConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class EventSortingTest {

    private fun sortEvents(events: List<CountdownConfig>, now: Long): List<CountdownConfig> {
        return events.sortedWith(
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

    @Test
    fun testUpcomingEventsSortedClosestFirst() {
        val now = 1000000L
        val eventSoon = CountdownConfig(id = "1", milestoneName = "Thi THPT", targetEpochMillis = now + 10000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = false)
        val eventLater = CountdownConfig(id = "2", milestoneName = "Tết 2027", targetEpochMillis = now + 50000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = false)
        val eventMid = CountdownConfig(id = "3", milestoneName = "Sinh nhật", targetEpochMillis = now + 20000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = false)

        val sorted = sortEvents(listOf(eventLater, eventSoon, eventMid), now)

        assertEquals("Thi THPT", sorted[0].milestoneName)
        assertEquals("Sinh nhật", sorted[1].milestoneName)
        assertEquals("Tết 2027", sorted[2].milestoneName)
    }

    @Test
    fun testUpcomingBeforeCountUpAndFinished() {
        val now = 1000000L
        val finishedCountdown = CountdownConfig(id = "1", milestoneName = "Đã xong", targetEpochMillis = now - 20000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = false)
        val countUpEvent = CountdownConfig(id = "2", milestoneName = "Yêu nhau", targetEpochMillis = now - 50000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = true)
        val upcomingEvent = CountdownConfig(id = "3", milestoneName = "Du lịch", targetEpochMillis = now + 10000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = false)

        val sorted = sortEvents(listOf(finishedCountdown, countUpEvent, upcomingEvent), now)

        assertEquals("Du lịch", sorted[0].milestoneName)       // Nhóm 0: Đang đếm ngược
        assertEquals("Yêu nhau", sorted[1].milestoneName)      // Nhóm 1: Ngày đã qua (Count-up)
        assertEquals("Đã xong", sorted[2].milestoneName)       // Nhóm 2: Đã kết thúc
    }

    @Test
    fun testCountUpSortedMostRecentFirst() {
        val now = 1000000L
        val recentCountUp = CountdownConfig(id = "1", milestoneName = "Nghỉ việc gần đây", targetEpochMillis = now - 10000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = true)
        val oldCountUp = CountdownConfig(id = "2", milestoneName = "Tốt nghiệp lâu rồi", targetEpochMillis = now - 90000L, notifyTimes = emptyList(), notifyEnabled = false, isCountUp = true)

        val sorted = sortEvents(listOf(oldCountUp, recentCountUp), now)

        assertEquals("Nghỉ việc gần đây", sorted[0].milestoneName)
        assertEquals("Tốt nghiệp lâu rồi", sorted[1].milestoneName)
    }
}
