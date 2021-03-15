package com.va.android.task.implementation.kotlin.location

import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.LocationRequest

/**
 * This class encapsulates some properties related to the Accuracy & Frequency of the location updates.
 *
 * <p>Use its nested [Builder] class to construct new instances.
 */
class LocationOptions private constructor(
        /**
         * Returns the interval (in milliseconds) at which location is computed for your app.
         */
        val interval: Long,

        /**
         * Returns the interval (in milliseconds) at which location computed for
         * other apps is delivered to your app.
         */
        val fastestInterval: Long,

        /**
         * Returns the accuracy (precision of the location data).
         */
        @Priority
        val priority: Int
) {
    @IntDef(
        Priority.PRIORITY_NO_POWER,
        Priority.PRIORITY_LOW_POWER,
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        Priority.PRIORITY_HIGH_ACCURACY
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Priority {
        companion object {
            const val PRIORITY_NO_POWER = LocationRequest.PRIORITY_NO_POWER
            const val PRIORITY_LOW_POWER = LocationRequest.PRIORITY_LOW_POWER
            const val PRIORITY_BALANCED_POWER_ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            const val PRIORITY_HIGH_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    /**
     * Builds instances of [LocationOptions].
     */
    @Suppress("unused")
    class Builder {
        companion object {
            @VisibleForTesting
            internal const val DEFAULT_INTERVAL = 10000L // 10 seconds

            @VisibleForTesting
            internal const val DEFAULT_FASTEST_INTERVAL = 5000L

            @Priority
            @VisibleForTesting
            internal const val DEFAULT_PRIORITY = Priority.PRIORITY_HIGH_ACCURACY
        }

        // Optional parameters - initialized to default values
        private var interval: Long = DEFAULT_INTERVAL
        private var fastestInterval: Long = DEFAULT_FASTEST_INTERVAL
        @Priority private var priority: Int = DEFAULT_PRIORITY

        /**
         * Specifies the [interval] in milliseconds at which location is computed for your app.
         */
        fun setInterval(interval: Long): Builder {
            this.interval = interval
            return this
        }

        /**
         * Specifies the [fastestInterval] in milliseconds at which location computed for
         * other apps is delivered to your app.
         */
        fun setFastestInterval(fastestInterval: Long): Builder {
            this.fastestInterval = fastestInterval
            return this
        }

        /**
         * Specifies the [priority], the location accuracy (precision of the location data).
         */
        fun setPriority(@Priority priority: Int): Builder {
            this.priority = priority
            return this
        }

        /**
         * Returns new instance of [LocationOptions].
         */
        fun build(): LocationOptions = LocationOptions(
                interval = interval,
                fastestInterval = fastestInterval,
                priority = priority
        )
    }
}