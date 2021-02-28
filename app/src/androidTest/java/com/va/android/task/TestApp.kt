package com.va.android.task

import com.va.android.task.implementation.java.App
import com.va.android.task.implementation.java.util.SimpleCountingIdlingResource

class TestApp : App() {
    override fun getIdlingResource(): SimpleCountingIdlingResource? = IDLING_RESOURCE
}

private val IDLING_RESOURCE = SimpleCountingIdlingResource("APP_RESOURCE")