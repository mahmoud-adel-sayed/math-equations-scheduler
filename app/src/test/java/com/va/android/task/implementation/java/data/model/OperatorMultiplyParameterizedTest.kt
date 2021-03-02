package com.va.android.task.implementation.java.data.model

import androidx.test.filters.SmallTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
@SmallTest
class OperatorMultiplyParameterizedTest(
        private val firstOperand: Double,
        private val secondOperand: Double,
        private val expectedResult: Double
) {
    companion object {
        @Parameters
        @JvmStatic
        fun data(): Iterable<Array<Any>> = listOf(
                arrayOf<Any>(0, 0, 0),
                arrayOf<Any>(3, 3, 9),
                arrayOf<Any>(3, -3, -9),
                arrayOf<Any>(-3, 3, -9),
                arrayOf<Any>(-3, -3, 9)
        )
    }

    @Test
    fun multiplyTwoNumbers() {
        val result = Operator.MULTIPLY.compute(firstOperand, secondOperand)
        assertThat(result, `is`(equalTo(expectedResult)))
    }
}