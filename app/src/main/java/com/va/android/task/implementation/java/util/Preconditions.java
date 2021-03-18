package com.va.android.task.implementation.java.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Preconditions {
    private Preconditions() { }

    /**
     * Ensures that an object reference is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    @NonNull
    public static <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference is not null.
     *
     * @param reference an object reference
     * @param error the exception message to use if the check fails
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    @NonNull
    public static <T> T checkNotNull(@Nullable T reference, @NonNull String error) {
        if (reference == null) {
            throw new NullPointerException(error);
        }
        return reference;
    }
}
