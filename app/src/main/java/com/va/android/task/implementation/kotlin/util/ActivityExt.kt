package com.va.android.task.implementation.kotlin.util

import android.content.Context
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast

fun Context.showToast(message: String, duration: Int) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Tries to parse the content of the [EditText] as double and throws [NumberFormatException] if the
 * content of the editText is empty or not a double value.
 */
fun EditText.toDouble(): Double {
    val operandText = text.toString()
    if (TextUtils.isEmpty(operandText)) {
        throw NumberFormatException("Cannot be empty!")
    }
    return operandText.toDouble()
}