package com.va.android.task.implementation.java.engine

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.va.android.task.BuildConfig.APPLICATION_ID
import com.va.android.task.R
import com.va.android.task.answer
import com.va.android.task.implementation.java.App
import com.va.android.task.implementation.java.engine.MathEngineService.ACTION_CANCEL_ALL
import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import com.va.android.task.implementation.java.engine.data.model.Operator
import com.va.android.task.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
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

    @After
    fun tearDown() {
        getApplicationContext<App>().sendBroadcast(Intent(ACTION_CANCEL_ALL))
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

    @Test
    fun foregroundNotification_withOnePendingAndNoResults() {
        val context = getApplicationContext<App>()
        val delayTime = 60L
        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, delayTime)
        serviceRule.startService(MathEngineService.createIntent(context, mathQuestion))

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()

        val content = context.getString(R.string.format_pending_finished_operations, 1, 0)
        device.wait(Until.hasObject(By.text(content)), WAIT_TIMEOUT)

        device.pressBack()
    }

    @Test
    fun foregroundNotification_withNoPendingAndOneResult() {
        val context = getApplicationContext<App>()
        val delayTime = 0L
        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, delayTime)
        serviceRule.startService(MathEngineService.createIntent(context, mathQuestion))

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()

        val content = context.getString(R.string.format_pending_finished_operations, 0, 1)
        device.wait(Until.hasObject(By.text(content)), WAIT_TIMEOUT)

        device.pressBack()
    }

    @Test
    fun foregroundNotification_clickingOnIt_opensTheApp() {
        val context = getApplicationContext<App>()
        val title = context.getString(R.string.label_math_engine_service)
        val content = context.getString(R.string.format_pending_finished_operations, 0, 1)

        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, 0L)
        serviceRule.startService(MathEngineService.createIntent(context, mathQuestion))

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()
        device.wait(Until.hasObject(By.text(content)), WAIT_TIMEOUT)

        val titleUiObject = device.findObject(By.text(title))
        val contentUiObject = device.findObject(By.text(content))

        assertEquals(title, titleUiObject.text)
        assertEquals(content, contentUiObject.text)

        contentUiObject.click()
        device.wait(Until.hasObject(By.pkg(APPLICATION_ID)), WAIT_TIMEOUT)
    }
}

private const val WAIT_TIMEOUT = 5000L