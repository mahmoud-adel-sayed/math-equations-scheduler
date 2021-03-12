package com.va.android.task.implementation.java.location;

import com.google.android.gms.location.LocationRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * This class encapsulates some properties related to the Accuracy & Frequency of the location updates.
 *
 * <p>Use its nested {@link LocationOptions.Builder} builder class to construct new instances.
 */
public final class LocationOptions {
    @IntDef({
            Priority.PRIORITY_NO_POWER,
            Priority.PRIORITY_LOW_POWER,
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            Priority.PRIORITY_HIGH_ACCURACY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {
        int PRIORITY_NO_POWER = LocationRequest.PRIORITY_NO_POWER;
        int PRIORITY_LOW_POWER = LocationRequest.PRIORITY_LOW_POWER;
        int PRIORITY_BALANCED_POWER_ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        int PRIORITY_HIGH_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    }

    private final long interval;
    private final long fastestInterval;
    @Priority private final int priority;

    private LocationOptions(Builder builder) {
        interval = builder.interval;
        fastestInterval = builder.fastestInterval;
        priority = builder.priority;
    }

    /**
     * Returns the interval at which location is computed for your app.
     *
     * @return The interval.
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Returns the interval at which location computed for other apps is delivered to your app.
     *
     * @return The fastest interval.
     */
    public long getFastestInterval() {
        return fastestInterval;
    }

    /**
     * Returns the accuracy (precision of the location data).
     *
     * @return The priority.
     */
    @Priority
    public int getPriority() {
        return priority;
    }

    /**
     * Builds instances of {@link LocationOptions}.
     */
    @SuppressWarnings("unused")
    public static class Builder {
        // Optional parameters - initialized to default values
        private long interval = 10000; // 10 seconds
        private long fastestInterval = 5000;
        @Priority private int priority = Priority.PRIORITY_HIGH_ACCURACY;

        public Builder() {
        }

        /**
         * Specifies the interval at which location is computed for your app.
         *
         * @param interval The interval
         * @return The builder instance.
         */
        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        /**
         * Specifies the interval at which location computed for other apps is delivered to your app.
         *
         * @param fastestInterval The fastest interval
         * @return The builder instance.
         */
        public Builder setFastestInterval(long fastestInterval) {
            this.fastestInterval = fastestInterval;
            return this;
        }

        /**
         * Specifies the location accuracy (precision of the location data).
         *
         * @param priority The priority
         * @return The builder instance.
         */
        public Builder setPriority(@Priority int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Returns new instance of LocationOptions.
         *
         * @return new instance of LocationOptions.
         */
        public LocationOptions build() {
            return new LocationOptions(this);
        }
    }
}
