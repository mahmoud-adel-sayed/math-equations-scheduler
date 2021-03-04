package com.va.android.task.implementation.kotlin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.va.android.task.R

fun Context.showSnackBar(
        root: View,
        @StringRes message: Int,
        @StringRes actionLabel: Int,
        listener: (v: View) -> Unit
) {
    val snackBar = Snackbar.make(root, getString(message), Snackbar.LENGTH_INDEFINITE)
    snackBar.setAction(getString(actionLabel), listener)
    snackBar.show()
    val tv = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv?.gravity = Gravity.CENTER_HORIZONTAL
}

fun Context.showToast(@StringRes stringResId: Int, duration: Int) {
    Toast.makeText(this, stringResId, duration).show()
}

fun EditText.toDouble(): Double {
    val operandText = text.toString()
    if (TextUtils.isEmpty(operandText)) {
        throw NumberFormatException("Cannot be empty!")
    }
    return operandText.toDouble()
}

fun Context.isLocationPermissionGranted(): Boolean {
    val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestLocationPermission(root: View, requestCode: Int) {
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale) {
        showSnackBar(root, R.string.location_permission_rationale, R.string.ok) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode)
        }
    } else {
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
        )
    }
}