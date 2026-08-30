package com.nvdung1607.countdayleave

import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteRepositoryTest {

    private fun getSafeIndex(index: Int, size: Int): Int {
        if (size == 0) return 0
        return ((index % size) + size) % size
    }

    @Test
    fun testNormalIndexWithinBounds() {
        val size = 50
        assertEquals(0, getSafeIndex(0, size))
        assertEquals(25, getSafeIndex(25, size))
        assertEquals(49, getSafeIndex(49, size))
    }

    @Test
    fun testOverflowIndexWrapsAround() {
        val size = 10
        assertEquals(0, getSafeIndex(10, size))
        assertEquals(2, getSafeIndex(12, size))
        assertEquals(5, getSafeIndex(25, size))
    }

    @Test
    fun testNegativeIndexWrapsAroundBackwards() {
        val size = 10
        // -1 should wrap to 9
        assertEquals(9, getSafeIndex(-1, size))
        // -10 should wrap to 0
        assertEquals(0, getSafeIndex(-10, size))
        // -11 should wrap to 9
        assertEquals(9, getSafeIndex(-11, size))
    }
}
