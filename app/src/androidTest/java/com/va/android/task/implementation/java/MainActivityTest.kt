package com.va.android.task.implementation.java

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.va.android.task.R
import com.va.android.task.RecyclerViewItemCountAssertion
import com.va.android.task.implementation.java.data.model.Operator
import com.va.android.task.nestedScrollTo
import com.va.android.task.selectTabAtPosition
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private var idlingResource: IdlingResource? = null
    private lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        // This is needed when operations are not scheduled in the main Looper.
        idlingResource = getApplicationContext<App>().idlingResource
        idlingResource?.let { IdlingRegistry.getInstance().register(it) }

        val activityScenario = launch(MainActivity::class.java)
        activityScenario.onActivity { activity ->
            mainActivity = activity
            // Disable animations for the pending operations & the operation results
            (activity.findViewById(R.id.rv_pending_operations) as RecyclerView).itemAnimator = null
            (activity.findViewById(R.id.rv_operations_results) as RecyclerView).itemAnimator = null
        }
    }

    @After
    fun tearDown() {
        idlingResource?.let { IdlingRegistry.getInstance().unregister(it) }
    }

    @Test
    fun noOperands_showsError() {
        // When there are no operands and the calculate button is clicked.
        onView(withId(R.id.et_first_operand)).perform(clearText())
        onView(withId(R.id.et_second_operand)).perform(clearText())
        onView(withId(R.id.btn_calculate)).perform(click())

        // Then a toast message will be shown with an error (Type valid operand)
        onView(withText(R.string.err_operand))
                .inRoot(withDecorView(not(mainActivity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun enteredOperands_noDelayTime_showsError() {
        // When operands entered but no delay time entered and the calculate button is clicked.
        onView(withId(R.id.et_first_operand)).perform(typeText("1"), closeSoftKeyboard())
        onView(withId(R.id.et_second_operand)).perform(typeText("1"), closeSoftKeyboard())
        onView(withId(R.id.btn_calculate)).perform(click())

        // Then a toast message will be shown with an error (Invalid delay time)
        onView(withText(R.string.err_invalid_delay_time))
                .inRoot(withDecorView(not(mainActivity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun inputsFilled_divisionByZero_showsError() {
        // When all inputs filled, but division by zero is detected.
        onView(withId(R.id.et_first_operand)).perform(replaceText("1"))
        onView(withId(R.id.et_second_operand)).perform(replaceText("0"))
        onView(withId(R.id.spinner_operators)).perform(click())
        onView(withText(Operator.DIVIDE.toString())).perform(click())
        onView(withId(R.id.et_delay_time)).perform(replaceText("1"))
        onView(withId(R.id.btn_calculate)).perform(click())

        // Then a toast message will be shown with an error (Division by zero is undefined)
        onView(withText(R.string.err_division_by_zero))
                .inRoot(withDecorView(not(mainActivity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun validInputs_communicatesCorrectlyWithTheBackgroundService() {
        val first = 2.0
        val second = 2.0
        val operator = Operator.ADD
        val delayTime = 5L
        val result = String.format(Locale.US, "%.2f %s %.2f = %.2f",
                first, operator.symbol(), second, operator.compute(first, second))

        // When all inputs filled and the calculate button is clicked.
        onView(withId(R.id.et_first_operand)).perform(replaceText(first.toString()))
        onView(withId(R.id.et_second_operand)).perform(replaceText(second.toString()))
        onView(withId(R.id.spinner_operators)).perform(click())
        onView(withText(operator.toString())).perform(click())
        onView(withId(R.id.et_delay_time)).perform(replaceText(delayTime.toString()))
        onView(withId(R.id.btn_calculate)).perform(click())

        // Wait for the service to handle the intent.
        Thread.sleep(200)

        // After the operation has finished.
        // Scroll to the tabs, select pending tab, check no pending operations.
        onView(withId(R.id.tabs)).perform(nestedScrollTo())
        onView(withId(R.id.tabs)).perform(selectTabAtPosition(0))
        onView(withId(R.id.rv_pending_operations)).check(RecyclerViewItemCountAssertion(0))

        // Select the results tab, check that there is one result.
        onView(withId(R.id.tabs)).perform(selectTabAtPosition(1))
        onView(withId(R.id.rv_operations_results)).check(RecyclerViewItemCountAssertion(1))

        // Check the correct result is displayed.
        onView(withId(R.id.rv_operations_results)).perform(nestedScrollTo())
        onView(withId(R.id.rv_operations_results))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(withText(result)).check(matches(isDisplayed()))
    }
}