package com.va.android.task.implementation.java.location

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class LocationOptionsTest {

    companion object {
        private const val INTERVAL = 1000L
        private const val FASTEST_INTERVAL = 500L
    }

    @Test
    fun builder_withDefaultValues() {
        val options = LocationOptions.Builder().build()

        assertEquals(options.priority, LocationOptions.Builder.DEFAULT_PRIORITY)
        assertEquals(options.interval, LocationOptions.Builder.DEFAULT_INTERVAL)
        assertEquals(options.fastestInterval, LocationOptions.Builder.DEFAULT_FASTEST_INTERVAL)
    }

    @Test
    fun builder_constructsObjects_correctly() {
        val options = LocationOptions.Builder()
                .setPriority(LocationOptions.Priority.PRIORITY_LOW_POWER)
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .build()

        assertThat(options.priority, `is`(equalTo(LocationOptions.Priority.PRIORITY_LOW_POWER)))
        assertThat(options.interval, `is`(equalTo(INTERVAL)))
        assertThat(options.fastestInterval, `is`(equalTo(FASTEST_INTERVAL)))
    }

    @Test
    fun builder_settingOnlyPriority() {
        val options = LocationOptions.Builder()
                .setPriority(LocationOptions.Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()

        assertEquals(options.priority, LocationOptions.Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        assertEquals(options.interval, LocationOptions.Builder.DEFAULT_INTERVAL)
        assertEquals(options.fastestInterval, LocationOptions.Builder.DEFAULT_FASTEST_INTERVAL)
    }

    @Test
    fun builder_settingOnlyInterval() {
        val options = LocationOptions.Builder()
                .setInterval(INTERVAL)
                .build()

        assertEquals(options.priority, LocationOptions.Builder.DEFAULT_PRIORITY)
        assertEquals(options.interval, INTERVAL)
        assertEquals(options.fastestInterval, LocationOptions.Builder.DEFAULT_FASTEST_INTERVAL)
    }

    @Test
    fun builder_settingOnlyFastestInterval() {
        val options = LocationOptions.Builder()
                .setFastestInterval(FASTEST_INTERVAL)
                .build()

        assertEquals(options.priority, LocationOptions.Builder.DEFAULT_PRIORITY)
        assertEquals(options.interval, LocationOptions.Builder.DEFAULT_INTERVAL)
        assertEquals(options.fastestInterval, FASTEST_INTERVAL)

    }

    @Test
    fun builder_withMixedValues() {
        // Setting priority & interval
        var options = LocationOptions.Builder()
                .setPriority(LocationOptions.Priority.PRIORITY_NO_POWER)
                .setInterval(INTERVAL)
                .build()

        assertEquals(options.priority, LocationOptions.Priority.PRIORITY_NO_POWER)
        assertEquals(options.interval, INTERVAL)
        assertEquals(options.fastestInterval, LocationOptions.Builder.DEFAULT_FASTEST_INTERVAL)

        // Setting priority & fastestInterval
        options = LocationOptions.Builder()
                .setPriority(LocationOptions.Priority.PRIORITY_LOW_POWER)
                .setFastestInterval(FASTEST_INTERVAL)
                .build()

        assertEquals(options.priority, LocationOptions.Priority.PRIORITY_LOW_POWER)
        assertEquals(options.interval, LocationOptions.Builder.DEFAULT_INTERVAL)
        assertEquals(options.fastestInterval, FASTEST_INTERVAL)

        // Setting interval & fastestInterval
        options = LocationOptions.Builder()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .build()

        assertEquals(options.priority, LocationOptions.Builder.DEFAULT_PRIORITY)
        assertEquals(options.interval, INTERVAL)
        assertEquals(options.fastestInterval, FASTEST_INTERVAL)
    }
}