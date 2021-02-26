package com.va.android.task.implementation.java;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.va.android.task.R;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerChannel();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void registerChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    getString(R.string.channel_engine_id),
                    getString(R.string.channel_engine_title),
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription(getString(R.string.channel_engine_desc));
            // Register the channel with the system
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(notificationChannel);
            }
        }
    }
}
