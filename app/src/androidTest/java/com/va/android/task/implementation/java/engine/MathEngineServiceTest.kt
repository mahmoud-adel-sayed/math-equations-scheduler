package com.va.android.task.implementation.java.engine

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import com.va.android.task.answer
import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import com.va.android.task.implementation.java.engine.data.model.Operator
import com.va.android.task.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(AndroidJUnit4::class)
class MathEngineServiceTest {

    @get:Rule
    var serviceRule = ServiceTestRule()

    private lateinit var service: MathEngineService

    @Before
    fun setup() {
        val intent = Intent(getApplicationContext(), MathEngineService::class.java)
        val binder = serviceRule.bindService(intent)
        service = (binder as MathEngineService.LocalBinder).service
        service.pending.clear()
        service.results.clear()
    }

    @Test
    fun listeners_calledCorrectly() {
        val listener = mock<MathEngineService.Listener>()
        service.addListener(listener)

        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, 2L)
        serviceRule.startService(MathEngineService.createIntent(getApplicationContext(), mathQuestion))

        // Test interaction with the listener
        verify(listener).onPendingOperationsChanged()
        verify(listener, never()).onResultsChanged()
        assertThat(service.pendingOperations.size, `is`(equalTo(1)))
        assertThat(service.operationsResults.size, `is`(equalTo(0)))
    }

    @Test
    fun mathEngineService_fullCycle() {
        val latch = CountDownLatch(3)
        service.addListener(object : MathEngineService.Listener {
            override fun onResultsChanged() {
                latch.countDown()
            }

            override fun onPendingOperationsChanged() {
                // Called twice
                latch.countDown()
            }
        })

        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, 5L)
        serviceRule.startService(MathEngineService.createIntent(getApplicationContext(), mathQuestion))

        assertTrue(latch.await(mathQuestion.delayTime + 1, TimeUnit.SECONDS))
        assertThat(service.pendingOperations.size, `is`(equalTo(0)))
        assertThat(service.operationsResults.size, `is`(equalTo(1)))
        assertEquals(service.operationsResults[0].result, mathQuestion.answer())
    }
}