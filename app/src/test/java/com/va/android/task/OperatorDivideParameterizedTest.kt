package com.va.android.task

import androidx.test.filters.SmallTest
import com.va.android.task.implementation.java.data.model.Operator
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
@SmallTest
class OperatorDivideParameterizedTest(
        private val firstOperand: Double,
        private val secondOperand: Double,
        private val expectedResult: Double
) {
    companion object {
        @Parameters
        @JvmStatic
        fun data(): Iterable<Array<Any>> = listOf(
                arrayOf<Any>(4, 4, 1),
                arrayOf<Any>(-4, -4, 1),
                arrayOf<Any>(2, -2, -1),
                arrayOf<Any>(0, 3, 0),
                arrayOf<Any>(5.0, 2.0, 2.5)
        )
    }

    @Test
    fun divideTwoNumbers() {
        val result = Operator.DIVIDE.compute(firstOperand, secondOperand)
        assertThat(result, `is`(equalTo(expectedResult)))
    }
}