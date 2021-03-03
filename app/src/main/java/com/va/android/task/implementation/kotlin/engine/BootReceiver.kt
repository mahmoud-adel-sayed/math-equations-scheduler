package com.va.android.task.implementation.kotlin.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * A Receiver to start the background service when device boots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ACTION_BOOT_COMPLETED == intent?.action) {
            MathEngineService.start(context!!)
        }
    }
}

private const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"