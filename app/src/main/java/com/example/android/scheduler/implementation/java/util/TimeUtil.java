package com.example.android.scheduler.implementation.java.util;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

/**
 * Utility class that handles simple time formatting.
 */
public final class TimeUtil {
    private TimeUtil() { }

    /**
     * Returns time in the following format {@literal "hh:mm:ss"}.
     *
     * @param hours The hours
     * @param minutes The minutes
     * @param seconds The seconds
     * @return The formatted time
     * @throws IllegalArgumentException if {@code hours}, {@code minutes}, or {@code seconds}
     * is negative
     */
    @NonNull
    public static String getTimeFormatted(long hours, long minutes, long seconds) {
        if (hours < 0) {
            throw new IllegalArgumentException("Hours can not be negative.");
        }
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes can not be negative.");
        }
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds can not be negative.");
        }
        return getDigits(hours) + ":" + getDigits(minutes) + ":" + getDigits(seconds);
    }

    @NonNull
    @VisibleForTesting
    static String getDigits(long number) {
        return (number < 10) ? "0" + number : String.valueOf(number);
    }
}
