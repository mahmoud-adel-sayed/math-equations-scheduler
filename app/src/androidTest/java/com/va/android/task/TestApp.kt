package com.va.android.task

import com.va.android.task.implementation.java.App
import com.va.android.task.implementation.java.util.SimpleCountingIdlingResource

class TestApp : App() {
    private val idlingRes by lazy { SimpleCountingIdlingResource("APP_RESOURCE") }

    override fun getIdlingResource(): SimpleCountingIdlingResource = idlingRes
}