package com.va.android.task.implementation.java.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A Receiver to start the background service when device boots.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MathEngineService.start(context);
        }
    }
}