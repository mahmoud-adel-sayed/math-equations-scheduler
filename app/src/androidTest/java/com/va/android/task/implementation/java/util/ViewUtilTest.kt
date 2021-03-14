package com.va.android.task.implementation.java.util

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.va.android.task.BuildConfig
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class ViewUtilTest {

    @Test(expected = NumberFormatException::class)
    fun getOperand_withEmptyContent_throwsException() {
        val editText = EditText(getApplicationContext())
        editText.text = null
        ViewUtil.getOperand(editText)
    }

    @Test(expected = NumberFormatException::class)
    fun getOperand_withNonParsableContent_throwsException() {
        val editText = EditText(getApplicationContext())
        editText.setText("non double value")
        ViewUtil.getOperand(editText)
    }

    @Test
    fun getOperand_withParsableContent_returnsDouble() {
        val editText = EditText(getApplicationContext())
        editText.setText(Math.PI.toString())
        val value: Double = ViewUtil.getOperand(editText)

        assertThat(value).isEqualTo(Math.PI)
    }

    @Test
    fun getAppSettingsIntent_hasAction_hasData_hasFlags() {
        val intent = ViewUtil.getAppSettingsIntent()

        assertNotNull(intent)
        assertThat(intent).hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        assertThat(intent).hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        assertThat(intent).hasData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null))
    }
}