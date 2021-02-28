package com.va.android.task.implementation.java.util;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingResource;

/**
 * An simple counter implementation of {@link IdlingResource} that determines idleness by
 * maintaining an internal counter. When the counter is 0 - it is considered to be idle, when it is
 * non-zero it is not idle.
 *
 * This class can then be used to wrap up operations that while in progress should block tests from
 * accessing the UI.
 */
public final class SimpleCountingIdlingResource implements IdlingResource {

    @Nullable
    private volatile ResourceCallback mCallback;
    private final AtomicInteger mCounter = new AtomicInteger(0);
    private final String mName;

    public SimpleCountingIdlingResource(@NonNull String name) {
        mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isIdleNow() {
        return mCounter.get() == 0;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

    public void increment() {
        mCounter.getAndIncrement();
    }

    public void decrement() {
        int value = mCounter.decrementAndGet();
        if (value == 0 && mCallback != null) {
            mCallback.onTransitionToIdle();
        }
    }
}