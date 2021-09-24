package com.example.android.scheduler.implementation.kotlin.location

import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.LocationRequest

/**
 * This class encapsulates some properties related to the Accuracy & Frequency of the location updates.
 *
 * @property interval The interval (in milliseconds) at which location is computed for your app.
 * @property fastestInterval The interval (in milliseconds) at which location computed for
 * other apps is delivered to your app.
 * @property priority The accuracy (precision of the location data).
 */
class LocationOptions(
    var interval: Long = DEFAULT_INTERVAL,
    var fastestInterval: Long = DEFAULT_FASTEST_INTERVAL,
    @Priority var priority: Int = DEFAULT_PRIORITY
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