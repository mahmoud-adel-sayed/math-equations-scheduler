package com.va.android.task.implementation.java.engine

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import com.va.android.task.implementation.java.data.model.MathQuestion
import com.va.android.task.implementation.java.data.model.Operator
import com.va.android.task.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(AndroidJUnit4::class)
class MathEngineServiceTest {

    @get:Rule
    var serviceRule = ServiceTestRule()

    @Test
    fun mathEngineService_fullCycle() {
        val service = bindToService()

        val latch = CountDownLatch(3)
        var listener: MathEngineService.Listener = object : MathEngineService.Listener {
            override fun onResultsChanged() {
                latch.countDown()
            }

            override fun onPendingOperationsChanged() {
                // Called twice
                latch.countDown()
            }
        }
        service.addListener(listener)

        val delay = 5L
        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, delay)
        serviceRule.startService(MathEngineService.createIntent(getApplicationContext(), mathQuestion))

        latch.await(delay + 1, TimeUnit.SECONDS)
        assertThat(service.pendingOperations.size, `is`(equalTo(0)))
        assertThat(service.operationsResults.size, `is`(equalTo(1)))
        assertEquals(service.operationsResults[0].result, answer(mathQuestion))

        // Reset
        service.results.clear()
        service.removeListener(listener)

        // Send another question
        listener = mock()
        service.addListener(listener)
        serviceRule.startService(MathEngineService.createIntent(getApplicationContext(), mathQuestion))

        // Test interaction with the listener
        verify(listener).onPendingOperationsChanged()
        verify(listener, never()).onResultsChanged()
        assertThat(service.pendingOperations.size, `is`(equalTo(1)))
        assertThat(service.operationsResults.size, `is`(equalTo(0)))
    }

    private fun bindToService(): MathEngineService {
        val intent = Intent(getApplicationContext(), MathEngineService::class.java)
        val binder = serviceRule.bindService(intent)
        return (binder as MathEngineService.LocalBinder).service
    }

    private fun answer(mathQuestion: MathQuestion) = String.format(
            Locale.US,
            "%.2f %s %.2f = %.2f",
            mathQuestion.firstOperand,
            mathQuestion.operator.symbol(),
            mathQuestion.secondOperand,
            mathQuestion.operator.compute(mathQuestion.firstOperand, mathQuestion.secondOperand)
    )
}