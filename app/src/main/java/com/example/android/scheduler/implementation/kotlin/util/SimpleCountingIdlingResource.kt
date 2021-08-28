package com.example.android.scheduler.implementation.kotlin.util

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback

import java.util.concurrent.atomic.AtomicInteger

/**
 * An simple counter implementation of [IdlingResource] that determines idleness by
 * maintaining an internal counter. When the counter is 0 - it is considered to be idle, when it is
 * non-zero it is not idle.
 *
 * This class can then be used to wrap up operations that while in progress should block tests from
 * accessing the UI.
 */
class SimpleCountingIdlingResource(private val name: String) : IdlingResource {

    @Volatile
    private var callback: ResourceCallback? = null
    private val counter = AtomicInteger(0)

    override fun getName(): String = name

    override fun isIdleNow(): Boolean = counter.get() == 0

    override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
        this.callback = callback
    }

    fun increment() {
        counter.getAndIncrement()
    }

    fun decrement() {
        val count = counter.decrementAndGet()
        if (count == 0) {
            callback?.onTransitionToIdle()
        }
    }
}