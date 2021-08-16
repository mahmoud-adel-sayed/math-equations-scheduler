package com.va.android.task.implementation.kotlin.location

import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.LocationRequest

/**
 * This class encapsulates some properties related to the Accuracy & Frequency of the location updates.
 */
class LocationOptions(
        /**
         * Returns the interval (in milliseconds) at which location is computed for your app.
         */
        var interval: Long = DEFAULT_INTERVAL,

        /**
         * Returns the interval (in milliseconds) at which location computed for
         * other apps is delivered to your app.
         */
        var fastestInterval: Long = DEFAULT_FASTEST_INTERVAL,

        /**
         * Returns the accuracy (precision of the location data).
         */
        @Priority
        var priority: Int = DEFAULT_PRIORITY
)

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

@VisibleForTesting
internal const val DEFAULT_INTERVAL = 10_000L // 10 seconds

@VisibleForTesting
internal const val DEFAULT_FASTEST_INTERVAL = 5000L

@Priority
@VisibleForTesting
internal const val DEFAULT_PRIORITY = Priority.PRIORITY_HIGH_ACCURACY