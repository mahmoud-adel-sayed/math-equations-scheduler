package com.va.android.task

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.github.tmurakami.dexopener.DexOpener

/**
 * A custom [AndroidJUnitRunner] used to replace the application used in tests with a
 * [TestApp].
 */
@Suppress("unused")
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        DexOpener.install(this)
        return super.newApplication(cl, TestApp::class.java.name, context)
    }
}