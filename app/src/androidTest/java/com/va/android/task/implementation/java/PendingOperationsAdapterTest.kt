package com.va.android.task.implementation.java

import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.va.android.task.TestActivity
import com.va.android.task.implementation.java.engine.data.model.MathQuestion
import com.va.android.task.implementation.java.engine.data.model.Operation
import com.va.android.task.implementation.java.engine.data.model.Operator
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class PendingOperationsAdapterTest {

    companion object {
        private val MATH_QUESTION =
                MathQuestion(1.0, 1.0, Operator.ADD, 10)
    }

    private lateinit var activityScenario: ActivityScenario<TestActivity>
    private lateinit var testActivity: TestActivity
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setup() {
        activityScenario = launch(TestActivity::class.java)
        activityScenario.onActivity { testActivity = it }

        recyclerView = RecyclerView(getApplicationContext()).apply {
            layoutManager = LinearLayoutManager(testActivity, RecyclerView.VERTICAL, false)
            itemAnimator = null
        }
        runOnUiThread {
            testActivity.addContentView(recyclerView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun timer_ticks() {
        val startTime = System.currentTimeMillis()
        val operations = listOf(
                Operation(startTime, startTime + MATH_QUESTION.delayTime * 1000, MATH_QUESTION)
        )
        runOnUiThread {
            recyclerView.adapter = PendingOperationsAdapter(testActivity.lifecycle, operations)
        }

        for (i in 1 until MATH_QUESTION.delayTime.toInt()) {
            val time = "Remaining Time: 00:00:0${MATH_QUESTION.delayTime.toInt() - i}"
            onView(withText(time)).check(matches(isDisplayed()))
            Thread.sleep(1000)
        }
    }

    @Test
    fun clearData_worksProperly() {
        val startTime = System.currentTimeMillis()
        val operations = mutableListOf(Operation(startTime, startTime + 1000, MATH_QUESTION))
        var adapter: PendingOperationsAdapter? = null
        runOnUiThread {
            adapter = PendingOperationsAdapter(testActivity.lifecycle, operations)
            recyclerView.adapter = adapter
        }
        val pendingAdapter = adapter!!

        assertThat(pendingAdapter.itemCount, `is`(equalTo(1)))
        pendingAdapter.clearData()
        assertThat(pendingAdapter.itemCount, `is`(equalTo(0)))
    }

    @Test
    fun replaceData_worksProperly() {
        val startTime = System.currentTimeMillis()
        val operations = mutableListOf(Operation(startTime, startTime + 1000, MATH_QUESTION))
        var adapter: PendingOperationsAdapter? = null
        runOnUiThread {
            adapter = PendingOperationsAdapter(testActivity.lifecycle, operations)
            recyclerView.adapter = adapter
        }

        val pendingAdapter = adapter!!
        operations.add(Operation(startTime, startTime + 10_000, MATH_QUESTION))
        pendingAdapter.replaceData(operations)

        assertThat(pendingAdapter.itemCount, `is`(equalTo(2)))
    }
}