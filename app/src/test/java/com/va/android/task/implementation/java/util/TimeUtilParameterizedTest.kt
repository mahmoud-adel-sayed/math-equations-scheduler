package com.va.android.task.implementation.java.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class TimeUtilParameterizedTest(
    private val hours: Long,
    private val minutes: Long,
    private val seconds: Long,
    private val expectedFormattedTime: String
) {
    companion object {
        @JvmStatic
        @Parameters(name = "{index}: TimeUtil.getTimeFormatted({0}, {1}, {2}) = {3}")
        fun data(): Iterable<Array<Any>> = listOf(
            arrayOf(0, 0, 0, "00:00:00"),
            arrayOf(1, 0, 0, "01:00:00"),
            arrayOf(0, 2, 0, "00:02:00"),
            arrayOf(0, 0, 10, "00:00:10"),
            arrayOf(1, 1, 9, "01:01:09"),
            arrayOf(10, 10, 10, "10:10:10"),
            arrayOf(11, 12, 13, "11:12:13")
        )
    }

    @Test
    fun getTimeFormatted_returnsCorrectFormat() {
        val time = TimeUtil.getTimeFormatted(hours, minutes, seconds)
        assertEquals(time, expectedFormattedTime)
    }
}