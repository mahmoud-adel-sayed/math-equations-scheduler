package com.va.android.task.implementation.java.engine

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.va.android.task.TestLifecycleOwner
import com.va.android.task.implementation.java.engine.data.model.MathAnswer
import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import com.va.android.task.implementation.java.engine.data.model.Operator
import com.va.android.task.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MediumTest
@RunWith(AndroidJUnit4::class)
class MathEngineTest {

    private lateinit var lifecycleOwner: TestLifecycleOwner

    @Before
    fun setup() {
        lifecycleOwner = TestLifecycleOwner()
    }

    @Test
    fun bindToService_thenUnbind() {
        runOnUiThread {
            val listener = mock<MathEngine.Listener>()
            val engine = MathEngine(getApplicationContext(), lifecycleOwner.lifecycle, listener)

            // When the lifecycle state changed to started, the engine binds to service
            lifecycleOwner.onStart()
            assertTrue(engine.isBound)

            // When the lifecycle state changed to stopped, the engine unbinds from service
            lifecycleOwner.onStop()
            assertFalse(engine.isBound)
        }
    }

    @Test
    fun bindingToService_onConnectedCalled() {
        val latch = CountDownLatch(1)
        val listener = mock<MathEngine.Listener>()
        `when`(listener.onConnected(anyList(), anyList())).then { latch.countDown() }
        runOnUiThread {
            MathEngine(getApplicationContext(), lifecycleOwner.lifecycle, listener)
            lifecycleOwner.onStart()
        }
        assertTrue(latch.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun calculate_worksCorrectly() {
        val bindingLatch = CountDownLatch(1)
        val interactionLatch = CountDownLatch(3)
        val listener = object : MathEngine.Listener {
            override fun onConnected(p: MutableList<MathQuestion>, r: MutableList<MathAnswer>) {
                bindingLatch.countDown()
            }

            override fun onPendingOperationsChanged(pending: MutableList<MathQuestion>) {
                // Called twice
                interactionLatch.countDown()
            }

            override fun onResultsChanged(results: MutableList<MathAnswer>) {
                interactionLatch.countDown()
            }
        }

        var engine: MathEngine? = null
        runOnUiThread {
            engine = MathEngine(getApplicationContext(), lifecycleOwner.lifecycle, listener)
            lifecycleOwner.onStart()
        }
        // Wait until bound
        assertTrue(bindingLatch.await(2, TimeUnit.SECONDS))

        // Call calculate and assert interactions
        val mathQuestion = MathQuestion(1.0, 1.0, Operator.ADD, 2L)
        engine?.calculate(mathQuestion)
        assertTrue(interactionLatch.await(mathQuestion.delayTime + 1, TimeUnit.SECONDS))
    }
}