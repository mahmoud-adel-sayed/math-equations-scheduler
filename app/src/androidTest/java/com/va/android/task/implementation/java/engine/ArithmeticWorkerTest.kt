package com.va.android.task.implementation.java.engine

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.testing.WorkManagerTestInitHelper
import com.va.android.task.WorkManagerTestRule
import com.va.android.task.answer
import com.va.android.task.implementation.java.engine.ArithmeticWorker.KEY_RESULT
import com.va.android.task.implementation.java.engine.ArithmeticWorker.getWorkInputData
import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import com.va.android.task.implementation.java.engine.data.model.Operator
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(AndroidJUnit4::class)
class ArithmeticWorkerTest {

    companion object {
        private val MATH_QUESTION =
                MathQuestion(1.0, 1.0, Operator.ADD, 5L)
    }

    @get:Rule
    var workRule = WorkManagerTestRule()

    @Test
    @Throws(Exception::class)
    fun noInputData_workerFails() {
        val request = OneTimeWorkRequestBuilder<ArithmeticWorker>().build()

        workRule.workManager.enqueue(request).result.get()
        val workInfo = workRule.workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    @Throws(Exception::class)
    fun validInputData_workerSucceeds() {
        val inputData = getWorkInputData(MATH_QUESTION)
        val request = OneTimeWorkRequestBuilder<ArithmeticWorker>()
                .setInputData(inputData)
                .build()

        workRule.workManager.enqueue(request).result.get()
        val workInfo = workRule.workManager.getWorkInfoById(request.id).get()
        val outputData = workInfo.outputData

        assertEquals(MATH_QUESTION.answer(), outputData.getString(KEY_RESULT))
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    @Throws(Exception::class)
    fun validInputData_withInitialDelay_workerSucceeds() {
        val inputData = getWorkInputData(MATH_QUESTION)
        val request = OneTimeWorkRequestBuilder<ArithmeticWorker>()
                .setInitialDelay(MATH_QUESTION.delayTime, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(workRule.targetContext)
        workRule.workManager.enqueue(request).result.get()
        // WorkManager initial delays are now met.
        testDriver?.setInitialDelayMet(request.id)

        val workInfo = workRule.workManager.getWorkInfoById(request.id).get()
        val outputData = workInfo.outputData

        assertEquals(MATH_QUESTION.answer(), outputData.getString(KEY_RESULT))
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }
}