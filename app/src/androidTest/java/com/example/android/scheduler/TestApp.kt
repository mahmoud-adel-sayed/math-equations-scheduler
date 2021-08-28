package com.example.android.scheduler

import com.example.android.scheduler.implementation.java.App
import com.example.android.scheduler.implementation.java.util.SimpleCountingIdlingResource

class TestApp : App() {
    private val idlingRes by lazy { SimpleCountingIdlingResource("APP_RESOURCE") }

    override fun getIdlingResource(): SimpleCountingIdlingResource = idlingRes
}