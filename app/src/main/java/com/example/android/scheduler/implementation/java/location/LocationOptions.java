package com.example.android.scheduler.implementation.java.location;

import androidx.annotation.IntDef;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.location.LocationRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
     * Returns the interval (in milliseconds) at which location is computed for your app.
     *
     * @return The interval (in milliseconds).
     *
     * @see #getFastestInterval()
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Returns the interval (in milliseconds) at which location computed for
     * other apps is delivered to your app.
     *
     * @return The fastest interval (in milliseconds).
     *
     * @see #getInterval()
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
        @VisibleForTesting
        static final long DEFAULT_INTERVAL = 10000; // 10 seconds

        @VisibleForTesting
        static final long DEFAULT_FASTEST_INTERVAL = 5000;

        @Priority
        @VisibleForTesting
        static final int DEFAULT_PRIORITY = Priority.PRIORITY_HIGH_ACCURACY;

        // Optional parameters - initialized to default values
        private long interval = DEFAULT_INTERVAL;
        private long fastestInterval = DEFAULT_FASTEST_INTERVAL;
        @Priority private int priority = DEFAULT_PRIORITY;

        public Builder() {
        }

        /**
         * Specifies the interval (in milliseconds) at which location is computed for your app.
         *
         * @param interval The interval (in milliseconds)
         * @return The builder instance.
         */
        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        /**
         * Specifies the interval (in milliseconds) at which location computed for
         * other apps is delivered to your app.
         *
         * @param fastestInterval The fastest interval (in milliseconds)
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
