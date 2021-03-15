package com.va.android.task.implementation.kotlin.util

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.Snackbar
import com.va.android.task.BuildConfig

@JvmOverloads
fun showSnackBar(
        root: View,
        @StringRes message: Int,
        @Duration duration: Int,
        actionLabel: String? = null,
        action: ((v: View) -> Unit)? = null
) {
    val snackBar = Snackbar.make(root, message, duration)
    if (actionLabel != null && action != null) {
        snackBar.setAction(actionLabel, action)
    }
    snackBar.show()
    val tv = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv?.gravity = Gravity.CENTER_HORIZONTAL
}

/**
 * Returns an intent that could be used to open the app details in the settings.
 *
 * @return The app settings intent.
 */
fun getAppSettingsIntent(): Intent = Intent().apply {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}