package com.va.android.task.implementation.kotlin

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.va.android.task.R
import com.va.android.task.implementation.kotlin.util.SimpleCountingIdlingResource

open class App : Application() {

    override fun onCreate() {
        super.onCreate()
        registerChannel()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun registerChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.channel_engine_id),
                getString(R.string.channel_engine_title),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = getString(R.string.channel_engine_desc)
            // Register the channel with the system
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(notificationChannel)
        }
    }

    // The Idling Resource which will be null in production.
    open fun getIdlingResource(): SimpleCountingIdlingResource? = null
}