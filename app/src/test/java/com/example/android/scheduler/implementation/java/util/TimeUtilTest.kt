package com.example.android.scheduler.implementation.java.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilTest {

    @Test(expected = IllegalArgumentException::class)
    fun getTimeFormatted_withNegativeHours_throwsException() {
        TimeUtil.getTimeFormatted(-1, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getTimeFormatted_withNegativeMinutes_throwsException() {
        TimeUtil.getTimeFormatted(0, -1, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getTimeFormatted_withNegativeSeconds_throwsException() {
        TimeUtil.getTimeFormatted(0, 0, -1)
    }

    @Test
    fun getTimeFormatted_returnsCorrectFormat() {
        val time = TimeUtil.getTimeFormatted(8, 6, 5)
        assertEquals(time, "08:06:05")
    }

    @Test
    fun getDigits_returnsCorrectFormat() {
        var digits = TimeUtil.getDigits(9)
        assertEquals(digits, "09")

        digits = TimeUtil.getDigits(10)
        assertEquals(digits, "10")

        digits = TimeUtil.getDigits(15)
        assertEquals(digits, "15")
    }
}